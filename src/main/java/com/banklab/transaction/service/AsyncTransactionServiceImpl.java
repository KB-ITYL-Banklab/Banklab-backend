package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.service.CategoryService;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionException;

@Service
@Log4j2
@RequiredArgsConstructor
public class AsyncTransactionServiceImpl implements AsyncTransactionService {
    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final SummaryBatchService summaryBatchService;
    private final RedisService redisService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Async
    public void getTransactions(long memberId, TransactionRequestDto request) {
        // 고도화 시 예외 처리 必
        if (request == null || request.getResAccount() == null || request.getResAccount().isBlank()) {
            throw new IllegalArgumentException("계좌 번호가 반드시 필요합니다.");
        }

        String accountNumber = request.getResAccount();
        String key = RedisKeyUtil.transaction(memberId, accountNumber);

        // Redis에 현재 계좌 처리 정보 존재 유무 확인 & 저장 (미존재시)
        boolean alreadyExists = redisService.setIfAbsent(key, "FETCHING_TRANSACTIONS", Duration.ofMinutes(5));

        if (alreadyExists) {
            log.info("이미 처리 중인 계좌입니다. {}", accountNumber);
            return;
        }
        try {
            AccountVO account = accountMapper.getAccountByAccountNumber(accountNumber);
            if (account == null) {
                log.warn("해당 계좌를 찾을 수 없습니다: {}", accountNumber);
                return;
            }

            // 0. 거래 내역 확인
            checkIsPresent(memberId, account, request);
            TransactionDTO dto = makeTransactionDTO(account, request);

            // 1. CODEF API 호출
            log.info("[START] 거래 내역 불러오기 시작, 계좌번호: {}",account.getResAccount());
            List<TransactionHistoryVO> transactions = TransactionResponse.requestTransactions(memberId, dto);
            if (transactions.isEmpty()) return;

            // 2. DB에 거래 내역 저장
            log.info("[START] 거래 내역 db 저장 시작, 계좌번호: {}", account.getResAccount());
            transactionService.saveTransactionList(memberId, account, transactions);
            log.info("[END] 거래 내역 db 저장 종료");

            // 3. 상호명 -> 카테고리 분류 실행
            boolean isCategorized = false;
            try {
                log.info("[START] 카테고리 분류 시작, 계좌번호: {}", account.getResAccount());
                redisService.set(key, "CLASSIFYING_CATEGORIES", 3);

                categoryService.categorizeTransactions(transactions, key);
                isCategorized = true;
            } catch (Exception e) {
                log.error("카테고리 분류 중 에러 발생", e);
            }

            // 4. 카테고리 분류 완료 후
            if (isCategorized) {
                // 4. 집계 업데이트
                log.info("[START] 집계 내역 db 저장 시작, 계좌번호: {}", account.getResAccount());
                redisService.set(key, "ANALYZING_DATA", 3);
                summaryBatchService.initDailySummary(memberId,request.getStartDate());
                log.info("[END] 집계 내역 db 저장 종료");

                redisService.set(key, "DONE", 1);
            } else {
                redisService.set(key, "FAILED", 1);
            }

        } catch (IOException | InterruptedException e) {
            log.error("거래 내역 불러오는 중 오류 발생");
            redisService.set(key, "FAILED", 1);
            throw new RuntimeException(e);
        } catch (CompletionException e) {
            log.error("카테고리 분류 비동기 처리 중  에러 발생");
            throw e;
        }
        log.info("[END] 모든 함수 종료, 계좌번호: {}", accountNumber);
    }


public void checkIsPresent(Long memberId, AccountVO account, TransactionRequestDto req) {
    LocalDate lastTransactionDate =
            transactionMapper.getLastTransactionDate(memberId, account.getResAccount());

    if (lastTransactionDate != null) {
        if (req == null) req = new TransactionRequestDto();
        req.setStartDate(lastTransactionDate.format(formatter));
    }
}

/**
 * @param account 계좌 정보
 * @param request 거래 내역 조회를 위한 요청 파라미터 (sDate, eDate, orderBy)
 * @return 거래 내역 조회를 위한 요청 DTO
 */
public TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request) {
    if (request == null) {
        request = new TransactionRequestDto();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(2);

        request.setStartDate(startDate.format(formatter)); // "20190601" 형식
        request.setEndDate(endDate.format(formatter));     // 오늘 날짜 형식
        request.setOrderBy("0");
    } else {
        if (request.getStartDate() == null || request.getStartDate().isEmpty()) {
            LocalDate defaultStartDate = LocalDate.now().minusYears(2);
            request.setStartDate(defaultStartDate.format(formatter));
        }
        if (request.getEndDate() == null || request.getEndDate().isEmpty()) {
            LocalDate defaultEndDate = LocalDate.now();
            request.setEndDate(defaultEndDate.format(formatter));
        }

        if (request.getOrderBy() == null || request.getOrderBy().isEmpty()) {
            request.setOrderBy("0");
        }
    }

    return TransactionDTO.builder()
            .account(account.getResAccount())
            .organization(account.getOrganization())
            .connectedId(account.getConnectedId())
            .orderBy(request.getOrderBy())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();
}




}
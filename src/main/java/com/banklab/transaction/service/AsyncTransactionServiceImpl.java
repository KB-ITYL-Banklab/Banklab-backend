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
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

    /**
     * 비동기적으로 특정 사용자의 한 계좌에 대한 거래 내역을 가져와서 처리합니다.
     * 전체 프로세스: CODEF API 호출 -> DB 저장 -> 카테고리 분류 -> 소비 내역 집계
     *
     * @param memberId 사용자 ID
     * @param request  거래 내역 조회 요청 DTO (계좌 번호 포함)
     */
    public void getTransactions(long memberId, TransactionRequestDto request) {
        if (request == null || request.getResAccount() == null || request.getResAccount().isBlank()) {
            throw new IllegalArgumentException("계좌 번호가 반드시 필요합니다.");
        }

        String accountNumber = request.getResAccount();
        String key = RedisKeyUtil.transaction(memberId, accountNumber);

        // Redis에 현재 계좌 처리 정보 존재 유무 확인 & 저장 (미존재시)
        boolean alreadyExists = redisService.setIfAbsent(key, "FETCHING_TRANSACTIONS", Duration.ofMinutes(5));

        if (alreadyExists) {
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

            // 1. CODEF API 호출 & 거래 내역이 없는 경우 return
            List<TransactionHistoryVO> transactions = TransactionResponse.requestTransactions(memberId, dto);
            if (transactions.isEmpty()) return;

            // 2. DB에 거래 내역 저장
            transactionService.saveTransactionList(memberId, account, transactions);


            // 3. 상호명 -> 카테고리 분류
            boolean isCategorized = false;
            try {
                redisService.set(key, "CLASSIFYING_CATEGORIES", 3);
                categoryService.categorizeTransactions(transactions, key);
                isCategorized = true;
            } catch (Exception e) {
                log.error("카테고리 분류 중 에러 발생", e);
            }

            // 4. 락 획득 및 집계 db 저장
            if (isCategorized) {

                int maxRetry = 3;
                int retryCount = 0;
                String lockKey = "lock:summary:" + memberId + ":" + account.getResAccount();
                String lockValue = UUID.randomUUID().toString();
                boolean locked = false;

                try {
                    // 4-1. 다른 작업이 집계 table 수정하고 있는 경우 대기
                    while (retryCount < maxRetry) {
                        locked = redisService.tryLock(lockKey, lockValue, 1); // 60초 락 유지
                        if (locked) {
                            break;
                        }
                        retryCount++;
                        log.warn("다른 작업이 집계 중입니다. {}초 후 재시도 {}/{}", account.getResAccount(), 30, retryCount, maxRetry);
                        Thread.sleep(30_000); // 30초 대기
                    }
                    // 4-2. 락 획득 실패 시 데이터 처리하지 않고 반환
                    if (!locked) {
                        return;
                    }

                    // 5. 락 획득한 경우 집계 table 저장
                    redisService.set(key, "ANALYZING_DATA", 2);
                    summaryBatchService.initDailySummary(memberId, account, request.getStartDate());

                    redisService.set(key, "DONE", 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 중 인터럽트 발생", e);
                    redisService.set(key, "FAILED", 1);
                } catch (Exception e) {
                    log.error("집계 작업 중 예외 발생", e);
                    redisService.set(key, "FAILED", 1);
                } finally {
                    if (locked) {
                        redisService.unlock(lockKey, lockValue);
                    }
                }
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
    }


    /**
     * DB에 저장된 마지막 거래 일자를 확인하여, 거래 내역 조회 시작일자를 설정합니다.
     * 중복 데이터 조회를 방지하기 위함입니다.
     *
     * @param memberId 사용자 ID
     * @param account  계좌 정보
     * @param req      거래 내역 조회 요청 DTO
     */
    public void checkIsPresent(Long memberId, AccountVO account, TransactionRequestDto req) {
        LocalDate lastTransactionDate =
                transactionMapper.getLastTransactionDate(memberId, account.getResAccount());

        if (lastTransactionDate != null) {
            if (req == null) req = new TransactionRequestDto();
            req.setStartDate(lastTransactionDate.format(formatter));
        }
    }

    /**
     * CODEF API 요청에 필요한 TransactionDTO를 생성합니다.
     * 요청 DTO에 날짜 정보가 없는 경우 기본값(최근 2년)을 설정합니다.
     *
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
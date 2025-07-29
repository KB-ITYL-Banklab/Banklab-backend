package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.kakaomap.service.KakaoMapService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class AsyncTransactionServiceImpl implements AsyncTransactionService {
    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final TransactionService transactionService;


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Async
    public void getTransactions(long memberId, TransactionRequestDto request){
        log.info("[START] 거래 내역 불러오기 시작 : Thread: {}",Thread.currentThread().getName());
        List<AccountVO> userAccounts=new ArrayList<>();

        // 계좌가 특정되지 않은 경우 (Batch 처리 || 전체 계좌 update)
        if(request==null||request.getResAccount().isBlank()){
            userAccounts= accountMapper.selectAccountsByUserId(memberId);
        }else{
            // 특정 계좌가 들어온 경우
            AccountVO account = accountMapper.getAccountByAccountNumber(request.getResAccount());
            userAccounts.add(account);
        }

        //2. 계좌별 거래 내역 조회 api 호출
        for (AccountVO account : userAccounts) {
            // 2-1. 해당 계좌의 거래 내역 존재 확인
            checkIsPresent(memberId, account, request);

            TransactionDTO dto = makeTransactionDTO(account, request);
            List<TransactionHistoryVO> transactions;

            try {
                // 3. CODEF API 호출
                transactions = TransactionResponse.requestTransactions(memberId,dto);
                // 4. DB에 거래 내역 저장
                transactionService.saveTransactionList(memberId,account, transactions );
            } catch (IOException | InterruptedException e) {
                log.error("거래 내역 불러오는 중 오류 발생");
                throw new RuntimeException(e);
            }
            log.info("[END] 모든 함수 종료: Thread: {}",Thread.currentThread().getName());
        }
    }

    public void checkIsPresent(Long memberId, AccountVO account, TransactionRequestDto req){
        LocalDate lastTransactionDate =
                transactionMapper.getLastTransactionDate(memberId, account.getResAccount());

        if(lastTransactionDate!=null){
            if (req == null) req = new TransactionRequestDto();
            req.setStartDate(lastTransactionDate.plusDays(1).format(formatter));
        }
    }

    /**
     *
     * @param account 계좌 정보
     * @param request 거래 내역 조회를 위한 요청 파라미터 (sDate, eDate, orderBy)
     * @return  거래 내역 조회를 위한 요청 DTO
     */
    private TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request){
        if(request == null){
            request = new TransactionRequestDto();
            LocalDate endDate   = LocalDate.now();
            LocalDate startDate = endDate.minusYears(2);

            request.setStartDate(startDate.format(formatter)); // "20190601" 형식
            request.setEndDate(endDate.format(formatter));     // 오늘 날짜 형식
            request.setOrderBy("0");
        }
        else{
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

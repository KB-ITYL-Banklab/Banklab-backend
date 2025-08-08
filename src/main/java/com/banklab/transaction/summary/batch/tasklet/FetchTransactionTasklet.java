package com.banklab.transaction.summary.batch.tasklet;

import com.banklab.account.dto.AccountDTO;
import com.banklab.account.service.AccountService;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.member.service.MemberService;
import com.banklab.member.service.MemberServiceImpl;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.service.AsyncTransactionService;
import com.banklab.transaction.service.TransactionService;
import com.banklab.transaction.service.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 모든 거래 내역 최신화 task
 */
@Component
@RequiredArgsConstructor
public class FetchTransactionTasklet implements Tasklet {

    private final MemberMapper memberMapper;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AsyncTransactionService asyncTransactionService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        
        // 모든 사용자 조회
        List<Long> allMemberIds = memberMapper.findAllMemberIds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");


        for(Long memberId : allMemberIds){
            List<String> accounts = accountService.getUserAccounts(memberId).stream()
                    .map(AccountDTO::getResAccount)
                    .toList();

            for(String account: accounts){
                // 1. 사용자 별 마지막 거래 내역 일자 구하기
                LocalDate lastTransactionDay = transactionService.getLastTransactionDay(memberId, account);

                LocalDate today = LocalDate.now();
                LocalDate lastDay = (lastTransactionDay != null)
                        ? lastTransactionDay.plusDays(1)
                        : today.minusYears(2);

                String startDate = lastDay.format(formatter);
                String endDate =today.format(formatter);


                // 2. 사용자의 각 계좌별 거래 내역 저장
                asyncTransactionService.getTransactions(
                        memberId,
                        TransactionRequestDto.builder()
                                .resAccount(account)
                                .startDate(startDate)
                                .endDate(endDate)
                                .orderBy("0")
                                .build()
                );
            }
        }
        return RepeatStatus.FINISHED;
    }
}

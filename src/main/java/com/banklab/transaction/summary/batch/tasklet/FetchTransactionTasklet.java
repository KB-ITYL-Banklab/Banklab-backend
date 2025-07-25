package com.banklab.transaction.summary.batch.tasklet;

import com.banklab.account.dto.AccountDTO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.account.service.AccountService;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.member.service.MemberService;
import com.banklab.member.service.MemberServiceImpl;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.service.TransactionService;
import com.banklab.transaction.service.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
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

    private final TransactionService transactionService;
    private final MemberMapper memberMapper;
    private final AccountService accountService;
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        List<Long> allMemberIds = memberMapper.findAllMemberIds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        for(Long memberId : allMemberIds){
            List<String> accounts = accountService.getUserAccounts(memberId).stream()
                    .map(AccountDTO::getResAccount)
                    .toList();

            for(String account : accounts){
                // 1. 특정 사용자의 특정 계좌 마지막 거래 내역 일자 구하기
                LocalDate lastTransactionDay = transactionService.getLastTransactionDay(memberId, account);
                LocalDate lastDay;
                LocalDate today =LocalDate.now();

                // 2. 거래 내역이 없는 경우, 현재로부터 2년 전 내역부터 가져오기
                lastDay = (lastTransactionDay!=null)
                        ?lastTransactionDay.plusDays(1)
                        :today.minusYears(2);

                String startDate = lastDay.format(formatter);
                String endDate =today.format(formatter);


                // 3. 사용자의 모든 계좌 거래 내역 저장하기
                transactionService.getTransactions(memberId,
                        TransactionRequestDto.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .orderBy("0")
                                .build());
            }
        }
        return RepeatStatus.FINISHED;
    }
}

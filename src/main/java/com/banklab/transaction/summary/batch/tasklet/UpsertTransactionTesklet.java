package com.banklab.transaction.summary.batch.tasklet;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.transaction.summary.mapper.SummaryMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * 집게 테이블에 저장하는 task
 */

@Component
@RequiredArgsConstructor
public class UpsertTransactionTesklet implements Tasklet {
    private final SummaryBatchService summaryBatchService;
    private final SummaryMapper summaryMapper;
    private final MemberMapper memberMapper;
    private final AccountMapper accountMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 1. 마지막 집계 일자 구하기 (임시로 null로 설정, 실제로는 계정 번호가 필요)
        LocalDate lastSummaryDate = null; // summaryMapper.getLastSummaryDate(accountNumber);
        List<Long> allMemberIds = memberMapper.findAllMemberIds();

        for(Long memberId : allMemberIds){
            List<AccountVO> accounts = accountMapper.selectAccountsByUserId(memberId);
        }



        LocalDate lastDay;
        LocalDate today = LocalDate.now();

        // 2. 집계 데이터가 없는 경우, 현재로부터 2년 전 내역부터 가져오기
        lastDay = (lastSummaryDate!=null)
                ?lastSummaryDate.plusDays(1)
                :today.minusYears(2);

        // 3. 마지막 일부터 오늘까지 집계테이블 저장
        while (!lastDay.isAfter(today)) {
            summaryBatchService.aggregateDailySummary(lastDay,null);
            lastDay = lastDay.plusDays(1);
        }
        return RepeatStatus.FINISHED;
    }
}

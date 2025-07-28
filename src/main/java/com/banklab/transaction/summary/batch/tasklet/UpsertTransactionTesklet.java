package com.banklab.transaction.summary.batch.tasklet;

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

/**
 * 집게 테이블에 저장하는 task
 */

@Component
@RequiredArgsConstructor
public class UpsertTransactionTesklet implements Tasklet {
    private final SummaryBatchService summaryBatchService;
    private final SummaryMapper summaryMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        
        // 1. 마지막 집계 일자 구하기
        LocalDate lastSummaryDate = summaryMapper.getLastSummaryDate();

        LocalDate lastDay;
        LocalDate today = LocalDate.now();

        // 2. 집계 데이터가 없는 경우, 현재로부터 10년 전 내역부터 가져오기
        lastDay = (lastSummaryDate!=null)
                ?lastSummaryDate.plusDays(1)
                :today.minusYears(10);

        // 3. 마지막 일부터 오늘까지 집계테이블 저장
        while (!lastDay.isAfter(today)) {
            summaryBatchService.aggregateDailySummary(lastDay);
            lastDay = lastDay.plusDays(1);
        }
        return RepeatStatus.FINISHED;
    }
}

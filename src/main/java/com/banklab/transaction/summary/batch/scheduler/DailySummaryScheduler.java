package com.banklab.transaction.summary.batch.scheduler;

import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
@Log4j2
public class DailySummaryScheduler {
    private final SummaryBatchService  summaryBatchService;

    public DailySummaryScheduler(SummaryBatchService summaryBatchService) {
        this.summaryBatchService = summaryBatchService;
    }

    // 매일 새벽 1시 실행(작성 전)
//    @Scheduled(cron = "*/10 * * * * ?")
////    public void Test(){
////        log.info("Start test DailySummaryScheduler");
////        LocalDate yesterdayLocalDate = LocalDate.now().minusDays(1);
////
////        // 2. LocalDate → java.util.Date 변환 (시:분:초는 00:00:00 으로 자동 설정됨)
////        Date yesterdayDate = Date.from(yesterdayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
////
////        // 3. 서비스 메서드 호출
////        summaryBatchService.aggregateDailySummary(yesterdayDate);
////        log.info("End test DailySummaryScheduler");
////    }
}

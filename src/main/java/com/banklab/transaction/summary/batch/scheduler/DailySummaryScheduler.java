package com.banklab.transaction.summary.batch.scheduler;

import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
@Log4j2
public class DailySummaryScheduler {

    private final JobLauncher jobLauncher;
    private final Job savingSummaryJob;

    public DailySummaryScheduler(
            JobLauncher jobLauncher,
            @Qualifier("savingSummaryJob") Job savingSummaryJob) {
        this.jobLauncher = jobLauncher;
        this.savingSummaryJob =savingSummaryJob;
    }

    /**
     * 전체 사용자 거래 내역 최신화 & 집계 테이블 최신화
     */
    // 매일 새벽 1시 실행(작성 전)
    @Scheduled(cron = "* * 0 * * ?")
    public void runDailySummaryBatch(){
        try {
            log.info("=== 거래 내역 & 집계 최신화 시작 ===");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("uuid", UUID.randomUUID().toString())
                    .toJobParameters();

            jobLauncher.run(savingSummaryJob, jobParameters);

            log.info("=== 거래 내역 최신화 & 집계 완료 ===");
        }catch (Exception e){
            log.error("거래 내역 최신화 & 집계 작업 중 오류 발생", e);
        }
    }
}

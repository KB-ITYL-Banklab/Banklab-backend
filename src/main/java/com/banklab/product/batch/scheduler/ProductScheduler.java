package com.banklab.product.batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("depositRefreshJob")
    private Job depositRefreshJob;

    @Autowired
    @Qualifier("savingsRefreshJob")
    private Job savingsRefreshJob;

    @Autowired
    @Qualifier("creditLoanRefreshJob")
    private Job creditLoanRefreshJob;

    /**
     * 예금 상품 배치 - 매일 오전 2시 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDepositBatch() {
        try {
            log.info("=== 예금 상품 배치 시작 (02:00) ===");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(depositRefreshJob, jobParameters);

            log.info("=== 예금 상품 배치 완료 ===");
            
        } catch (Exception e) {
            log.error("예금 상품 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 적금 상품 배치 - 매일 오전 2시 5분 실행
     */
    @Scheduled(cron = "0 5 2 * * *")
    public void runSavingsBatch() {
        try {
            log.info("=== 적금 상품 배치 시작 (02:05) ===");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(savingsRefreshJob, jobParameters);
            
            log.info("=== 적금 상품 배치 완료 ===");
            
        } catch (Exception e) {
            log.error("적금 상품 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 신용대출 상품 배치 - 매일 오전 2시 10분 실행
     */
    @Scheduled(cron = "0 10 2 * * *")
    public void runCreditLoanBatch() {
        try {
            log.info("=== 신용대출 상품 배치 시작 (02:10) ===");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(creditLoanRefreshJob, jobParameters);
            
            log.info("=== 신용대출 상품 배치 완료 ===");
            
        } catch (Exception e) {
            log.error("신용대출 상품 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 테스트용 - 매분 실행 (개발 중에만 사용)
     * 운영 환경에서는 주석 처리하세요
     */
//    // @Scheduled(fixedRate = 60000) // 1분마다
//    public void runTestBatch() {
//        log.info("=== 테스트 배치 실행 ===");
//        // 필요시 여기서 테스트 로직 실행
//    }
}

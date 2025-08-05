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

    @Autowired
    @Qualifier("annuityRefreshJob")
    private Job annuityRefreshJob;

    @Autowired
    @Qualifier("mortgageLoanRefreshJob")
    private Job mortgageLoanRefreshJob;

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

    @Scheduled(cron = "0 15 2 * * *")
    public void runAnnuityBatch() {
        try {
            log.info("=== 연금저축 상품 배치 시작 (02:15) ===");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(annuityRefreshJob, jobParameters);

            log.info("=== 연금저축 상품 배치 완료 ===");

        } catch (Exception e) {
            log.error("연금저축 상품 배치 실행 중 오류 발생", e);
        }
    }
    @Scheduled(cron = "0 20 2 * * *")
    public void runMortgageLoanBatch() {
        try {
            log.info("=== 주택담보대출 상품 배치 시작 (02:15) ===");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(mortgageLoanRefreshJob, jobParameters);

            log.info("=== 주택담보대출 상품 배치 완료 ===");

        } catch (Exception e) {
            log.error("주택담보대출 상품 배치 실행 중 오류 발생", e);
        }
    }
}

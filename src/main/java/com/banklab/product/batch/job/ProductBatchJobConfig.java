package com.banklab.product.batch.job;

import com.banklab.product.batch.tasklet.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 예금, 적금, 신용대출 상품들을 위한 배치 작업입니다.
 * 상품을 갱신하고, 이전 상품 정보들을 삭제합니다.
 * 이후 외부 API를 이용하여 새로운 데이터를 받아옵니다.
 */
@Configuration
public class ProductBatchJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // Deposit Tasklets
    @Autowired
    private DeleteDepositTasklet deleteDepositTasklet;

    @Autowired
    private FetchAndInsertDepositTasklet fetchAndInsertDepositTasklet;

    // Savings Tasklets
    @Autowired
    private DeleteSavingsTasklet deleteSavingsTasklet;

    @Autowired
    private FetchAndInsertSavingsTasklet fetchAndInsertSavingsTasklet;

    // Credit Loan Tasklets
    @Autowired
    private DeleteCreditLoanTasklet deleteCreditLoanTasklet;

    @Autowired
    private FetchAndInsertCreditLoanTasklet fetchAndInsertCreditLoanTasklet;

    @Bean
    public Job depositRefreshJob() {
        return jobBuilderFactory.get("depositRefreshJob")
                .start(deleteDepositStep())
                .next(fetchAndInsertDepositStep())
                .build();
    }

    @Bean
    public Step deleteDepositStep() {
        return stepBuilderFactory.get("deleteDepositStep")
                .tasklet(deleteDepositTasklet)
                .build();
    }

    @Bean
    public Step fetchAndInsertDepositStep() {
        return stepBuilderFactory.get("fetchAndInsertDepositStep")
                .tasklet(fetchAndInsertDepositTasklet)
                .build();
    }

    @Bean
    public Job savingsRefreshJob() {
        return jobBuilderFactory.get("savingsRefreshJob")
                .start(deleteSavingsStep())
                .next(fetchAndInsertSavingsStep())
                .build();
    }

    @Bean
    public Step deleteSavingsStep() {
        return stepBuilderFactory.get("deleteSavingsStep")
                .tasklet(deleteSavingsTasklet)
                .build();
    }

    @Bean
    public Step fetchAndInsertSavingsStep() {
        return stepBuilderFactory.get("fetchAndInsertSavingsStep")
                .tasklet(fetchAndInsertSavingsTasklet)
                .build();
    }

    @Bean
    public Job creditLoanRefreshJob() {
        return jobBuilderFactory.get("creditLoanRefreshJob")
                .start(deleteCreditLoanStep())
                .next(fetchAndInsertCreditLoanStep())
                .build();
    }

    @Bean
    public Step deleteCreditLoanStep() {
        return stepBuilderFactory.get("deleteCreditLoanStep")
                .tasklet(deleteCreditLoanTasklet)
                .build();
    }

    @Bean
    public Step fetchAndInsertCreditLoanStep() {
        return stepBuilderFactory.get("fetchAndInsertCreditLoanStep")
                .tasklet(fetchAndInsertCreditLoanTasklet)
                .build();
    }
}

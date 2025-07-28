package com.banklab.transaction.summary.batch.job;

import com.banklab.transaction.summary.batch.tasklet.FetchTransactionTasklet;
import com.banklab.transaction.summary.batch.tasklet.UpsertTransactionTesklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SummaryBatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FetchTransactionTasklet fetchTransactionTasklet;
    private final UpsertTransactionTesklet upsertTransactionTesklet;


    @Bean
    public Job savingSummaryJob(){
        return jobBuilderFactory.get("savingSummaryJob")
                .start(fetchTransactionStep())
                .next(upsertTransactionStep())
                .build();
    }

    @Bean
    public Step fetchTransactionStep(){
        return stepBuilderFactory.get("fetchTransactionStep")
                .tasklet(fetchTransactionTasklet)
                .build();
    }

    @Bean Step upsertTransactionStep(){
        return stepBuilderFactory.get("upsertTransactionStep")
                .tasklet(upsertTransactionTesklet)
                .build();
    }
}

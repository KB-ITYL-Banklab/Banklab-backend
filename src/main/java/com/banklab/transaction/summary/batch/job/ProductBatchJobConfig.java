package com.banklab.transaction.summary.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProductBatchJobConfig {


    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

}

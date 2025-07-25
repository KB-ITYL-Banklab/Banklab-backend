package com.banklab.product.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration("productSchedulerConfig")
public class SchedulerConfig {
    // 스케줄링 활성화 설정
}

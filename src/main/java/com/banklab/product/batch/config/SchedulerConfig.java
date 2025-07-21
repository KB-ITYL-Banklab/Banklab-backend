package com.banklab.product.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 스케줄링 활성화 설정
    // @Scheduled 어노테이션을 사용할 수 있게 해줍니다
}

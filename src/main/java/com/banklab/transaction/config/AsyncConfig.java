package com.banklab.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 비동기 작업용 Executor 설정
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);      // 최소 10개
        executor.setMaxPoolSize(30);       // 최대 30개
        executor.setQueueCapacity(200);    // 대기열 여유 증가
        executor.setThreadNamePrefix("AsyncThread-");
        executor.setKeepAliveSeconds(60);  // 유휴 스레드 정리 시간

        // Rejected 시 작업을 호출한 스레드가 직접 처리도록 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}

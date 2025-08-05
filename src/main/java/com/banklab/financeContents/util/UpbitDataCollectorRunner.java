package com.banklab.financeContents.util;

import com.banklab.financeContents.scheduler.UpbitDataScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 애플리케이션 시작 시 업비트 데이터 수집 실행
 * (필요시 활성화)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpbitDataCollectorRunner {

    private final UpbitDataScheduler upbitDataScheduler;

    /**
     * 애플리케이션 시작 시 한 번 실행
     * (주석 해제하여 사용)
     */
    // @PostConstruct
    public void initializeUpbitData() {
        log.info("=== 애플리케이션 시작 시 업비트 데이터 초기 수집 ===");
        
        try {
            // 시작 시 한 번 데이터 수집
            upbitDataScheduler.manualCollectUpbitData();
            log.info("=== 업비트 데이터 초기 수집 완료 ===");
        } catch (Exception e) {
            log.error("=== 업비트 데이터 초기 수집 실패 ===", e);
        }
    }
}

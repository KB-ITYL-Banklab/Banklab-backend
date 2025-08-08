package com.banklab.financeContents.scheduler;

import com.banklab.financeContents.service.UpbitDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 업비트 데이터 수집 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpbitDataScheduler {

    private final UpbitDataService upbitDataService;

    /**
     * 매일 오전 9시에 업비트 데이터 수집
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void collectDailyUpbitData() {
        log.info("=== 업비트 일일 데이터 수집 스케줄러 시작 ===");
        
        try {
            upbitDataService.collectAndSaveUpbitData();
            log.info("=== 업비트 일일 데이터 수집 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("=== 업비트 일일 데이터 수집 스케줄러 실패 ===", e);
        }
    }

    /**
     * 테스트용: 매 10분마다 실행 (필요시 주석 해제)
     */
    // @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void collectUpbitDataForTest() {
        log.info("=== 업비트 데이터 수집 테스트 스케줄러 시작 ===");
        
        try {
            upbitDataService.collectAndSaveUpbitData();
            log.info("=== 업비트 데이터 수집 테스트 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("=== 업비트 데이터 수집 테스트 스케줄러 실패 ===", e);
        }
    }

    /**
     * 수동 실행용 메서드
     */
    public void manualCollectUpbitData() {
        log.info("=== 업비트 데이터 수동 수집 시작 ===");
        
        try {
            upbitDataService.collectAndSaveUpbitData();
            log.info("=== 업비트 데이터 수동 수집 완료 ===");
        } catch (Exception e) {
            log.error("=== 업비트 데이터 수동 수집 실패 ===", e);
            throw e;
        }
    }
}

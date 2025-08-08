package com.banklab.financeContents.scheduler;

import com.banklab.financeContents.service.FinanceStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주식 데이터 자동 수집 스케줄러
 * 
 * 이 스케줄러는 정기적으로 공공데이터 API에서 주식 정보를 수집하여 
 * 데이터베이스에 저장합니다.
 * 
 * 실행 조건:
 * - application.properties에서 finance.stock.scheduler.enabled=true로 설정시에만 동작
 */
@Component
public class FinanceStockScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(FinanceStockScheduler.class);
    
    @Autowired
    private FinanceStockService financeStockService;
    
    @Value("${finance.stock.scheduler.enabled:false}")
    private boolean schedulerEnabled;
    
    /**
     * 매일 오전 9시에 전일 주식 데이터 수집 (상위 200개)
     * 
     * 크론 표현식: 0 0 9 * * MON-FRI (월-금요일 오전 9시)
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void collectDailyStockData() {
        if (!schedulerEnabled) {
            log.debug("주식 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        try {
            log.info("🕘 [스케줄러] 일일 상위 200개 종목 데이터 수집 시작 - {}", LocalDateTime.now());
            
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate targetDate = getLastBusinessDay(yesterday);
            
            log.info("📅 수집 대상 날짜: {}", targetDate);
            
            // 오래된 데이터 먼저 삭제 (30일 이전)
            int deletedCount = financeStockService.deleteOldData();
            log.info("🗑️ [스케줄러] 30일 이전 오래된 데이터 {}건 삭제", deletedCount);
            
            // 상위 200개 종목 데이터 저장
            int savedCount = financeStockService.saveTopStockDataFromApi(targetDate, 200);
            
            if (savedCount > 0) {
                log.info("✅ [스케줄러] 일일 상위 200개 종목 데이터 수집 완료: {}건 ({})", savedCount, targetDate);
            } else {
                log.warn("⚠️ [스케줄러] 일일 주식 데이터 수집 실패: 데이터 없음 ({})", targetDate);
            }
            
        } catch (Exception e) {
            log.error("❌ [스케줄러] 일일 주식 데이터 수집 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매주 일요일 오전 6시에 최근 30일 데이터 일괄 수집
     * 
     * 크론 표현식: 0 0 6 * * SUN (일요일 오전 6시)
     */
    @Scheduled(cron = "0 0 6 * * SUN")
    public void collectRecentStockData() {
        if (!schedulerEnabled) {
            log.debug("주식 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        try {
            log.info("🕕 [스케줄러] 주간 최근 30일 데이터 일괄 수집 시작 - {}", LocalDateTime.now());
            
            // 오래된 데이터 삭제
            int deletedCount = financeStockService.deleteOldData();
            log.info("🗑️ [스케줄러] 30일 이전 오래된 데이터 {}건 삭제", deletedCount);
            
            // 최근 30일 상위 200개 종목 데이터 저장
            int savedCount = financeStockService.saveRecentStockData(30, 200);
            
            if (savedCount > 0) {
                log.info("✅ [스케줄러] 주간 최근 30일 데이터 수집 완료: {}건", savedCount);
            } else {
                log.warn("⚠️ [스케줄러] 주간 데이터 수집 실패: 데이터 없음");
            }
            
        } catch (Exception e) {
            log.error("❌ [스케줄러] 주간 데이터 수집 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매일 오후 6시에 당일 주식 데이터 업데이트 (상위 100개)
     * 
     * 크론 표현식: 0 0 18 * * MON-FRI (월-금요일 오후 6시)
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void updateTodayStockData() {
        if (!schedulerEnabled) {
            log.debug("주식 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        try {
            log.info("🕕 [스케줄러] 당일 상위 100개 종목 데이터 업데이트 시작 - {}", LocalDateTime.now());
            
            LocalDate today = LocalDate.now();
            
            if (today.getDayOfWeek().getValue() >= 6) {
                log.info("📅 주말이므로 당일 업데이트를 건너뜀");
                return;
            }
            
            // 당일 데이터는 상위 100개만 업데이트
            int savedCount = financeStockService.saveTopStockDataFromApi(today, 100);
            
            if (savedCount > 0) {
                log.info("✅ [스케줄러] 당일 상위 100개 종목 데이터 업데이트 완료: {}건 ({})", savedCount, today);
            } else {
                log.warn("⚠️ [스케줄러] 당일 주식 데이터 업데이트 실패: 데이터 없음 ({})", today);
            }
            
        } catch (Exception e) {
            log.error("❌ [스케줄러] 당일 주식 데이터 업데이트 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매주 토요일 오전 10시에 주요 종목 데이터 재수집
     * 
     * 크론 표현식: 0 0 10 * * SAT (토요일 오전 10시)
     */
    @Scheduled(cron = "0 0 10 * * SAT")
    public void collectMajorStockData() {
        if (!schedulerEnabled) {
            log.debug("주식 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        try {
            log.info("🕙 [스케줄러] 주요 종목 데이터 재수집 시작 - {}", LocalDateTime.now());
            
            String[] majorStocks = {
                "005930", // 삼성전자
                "035420", // 네이버
                "005380", // 현대차
                "035720", // 카카오
                "000150", // 두산
                "000660", // SK하이닉스
                "051910", // LG화학
                "006400", // 삼성SDI
                "207940", // 삼성바이오로직스
                "068270"  // 셀트리온
            };
            
            int successCount = 0;
            for (String stockCode : majorStocks) {
                try {
                    boolean success = financeStockService.saveStockByCode(stockCode);
                    if (success) {
                        successCount++;
                    }
                    
                    Thread.sleep(1000); // 1초 대기 (API 호출 간격 조절)
                    
                } catch (Exception e) {
                    log.warn("⚠️ 주요 종목 {} 수집 실패: {}", stockCode, e.getMessage());
                }
            }
            
            log.info("✅ [스케줄러] 주요 종목 데이터 재수집 완료: {}/{}개", successCount, majorStocks.length);
            
        } catch (Exception e) {
            log.error("❌ [스케줄러] 주요 종목 데이터 재수집 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매월 1일 오전 2시에 오래된 데이터 정리 (선택사항)
     * 
     * 크론 표현식: 0 0 2 1 * * (매월 1일 오전 2시)
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void cleanupOldData() {
        if (!schedulerEnabled) {
            log.debug("주식 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        try {
            log.info("🧹 [스케줄러] 오래된 주식 데이터 정리 시작 - {}", LocalDateTime.now());
            
            // 6개월 전 날짜 계산
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            
            // 실제 정리 로직은 필요에 따라 구현
            // 예: financeStockService.deleteOldData(sixMonthsAgo);
            
            log.info("📅 {}일 이전 데이터 정리 대상", sixMonthsAgo);
            log.info("ℹ️ [스케줄러] 데이터 정리 로직은 필요시 구현 예정");
            
        } catch (Exception e) {
            log.error("❌ [스케줄러] 오래된 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 마지막 영업일 계산
     * 주말인 경우 금요일을 반환
     * 
     * @param date 기준 날짜
     * @return 마지막 영업일
     */
    private LocalDate getLastBusinessDay(LocalDate date) {
        // 월요일(1) ~ 금요일(5)은 그대로 반환
        // 토요일(6)이면 금요일(-1일)
        // 일요일(7)이면 금요일(-2일)
        
        int dayOfWeek = date.getDayOfWeek().getValue();
        
        if (dayOfWeek <= 5) {
            // 평일이면 그대로 반환
            return date;
        } else if (dayOfWeek == 6) {
            // 토요일이면 금요일로
            return date.minusDays(1);
        } else {
            // 일요일이면 금요일로
            return date.minusDays(2);
        }
    }
}

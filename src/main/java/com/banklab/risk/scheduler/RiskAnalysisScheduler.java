package com.banklab.risk.scheduler;

import com.banklab.risk.service.RiskAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 위험도 분석 스케줄러
 * 정기적으로 상품 위험도를 재분석합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RiskAnalysisScheduler {

    private final RiskAnalysisService riskAnalysisService;
    
    /**
     * 매일 새벽 2시 30분에 오늘 업데이트된 상품의 위험도를 재분석 (최적화)
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void scheduledTodayUpdatedRiskAnalysis() {
        log.info("정기 위험도 분석 시작 (오늘 업데이트된 상품만)...");
        try {
            riskAnalysisService.batchAnalyzeTodayUpdatedProductsRisk();
            log.info("정기 위험도 분석 완료 (오늘 업데이트된 상품만)");
        } catch (Exception e) {
            log.error("정기 위험도 분석 중 오류 발생 (오늘 업데이트된 상품만)", e);
        }
    }
    
    /**
     * 매주 일요일 새벽 3시마다 전체 위험도 재분석
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void scheduledFullRiskAnalysis() {
        log.info("주간 전체 위험도 분석 시작...");
        try {
            riskAnalysisService.batchAnalyzeAllProductsRisk();
            log.info("주간 전체 위험도 분석 완료");
        } catch (Exception e) {
            log.error("주간 전체 위험도 분석 중 오류 발생", e);
        }
    }
}

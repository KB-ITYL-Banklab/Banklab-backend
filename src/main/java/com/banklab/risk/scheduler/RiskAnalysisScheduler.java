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
     * 매일 새벽 2시 30분 에 모든 상품의 위험도를 재분석
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void scheduledRiskAnalysis() {
        log.info("정기 위험도 분석 시작...");
        try {
            riskAnalysisService.batchAnalyzeAllProductsRisk();
            log.info("정기 위험도 분석 완료");
        } catch (Exception e) {
            log.error("정기 위험도 분석 중 오류 발생", e);
        }
    }
}

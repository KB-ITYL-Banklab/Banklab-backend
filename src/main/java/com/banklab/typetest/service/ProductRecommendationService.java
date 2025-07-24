package com.banklab.typetest.service;

import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.risk.domain.RiskLevel;

import java.util.List;

public interface ProductRecommendationService {
    /**
     * 투자성향에 따른 추천상품 목록을 반환합니다.
     *
     * @param investmentTypeId 투자성향 ID (1: 안정형, 2: 중립형, 3: 공격형)
     * @return 추천상품 목록
     */
    List<RecommendedProductDTO> getRecommendedProducts(Long investmentTypeId);
    
    /**
     * 투자성향 ID를 RiskLevel로 매핑합니다.
     *
     * @param investmentTypeId 투자성향 ID
     * @return 매핑된 RiskLevel
     */
    RiskLevel mapInvestmentTypeToRiskLevel(Long investmentTypeId);
}

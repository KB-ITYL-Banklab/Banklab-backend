package com.banklab.product.service;

import com.banklab.product.dto.ProductRateInfo;
import com.banklab.risk.domain.ProductRiskRating;

import java.util.List;
import java.util.Map;

/**
 * 상품 금리 조회 서비스
 */
public interface ProductRateService {
    
    /**
     * 여러 상품의 금리 정보를 배치로 조회
     * @param ratings 상품 위험도 평가 목록
     * @return 상품 키별 금리 정보 맵
     */
    Map<String, ProductRateInfo> getBatchProductRates(List<ProductRiskRating> ratings);
}

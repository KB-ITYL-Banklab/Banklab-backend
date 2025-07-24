package com.banklab.typetest.dto;

import com.banklab.product.domain.ProductType;
import com.banklab.risk.domain.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductDTO {
    private Long productId;
    private ProductType productType;
    private String productName;
    private String companyName;
    private RiskLevel riskLevel;
    private String riskReason;
    
    // 상품 타입별 추가 정보
    private String interestRate;    // 금리 정보
    private String productFeature; // 상품 특징
    private String targetCustomer; // 대상 고객

    //상품 조회용 필드
    private String dclsMonth;   // 공시월
    private String finCoNo;     // 금융회사 코드
    private String finPrdtCd;
}

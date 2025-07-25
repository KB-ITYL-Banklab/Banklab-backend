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
    
    // 실제 금리 정보
    private String interestRate;    // 실제 금리 정보 (예: "연 2.50~2.50%" 또는 "연 2.45~2.50%")

    //상품 조회용 필드
    private String dclsMonth;   // 공시월
    private String finCoNo;     // 금융회사 코드
    private String finPrdtCd;   // 금융상품 코드
}

package com.banklab.risk.domain;

import com.banklab.product.domain.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRiskRating {
    private Long id;
    private ProductType productType; // DEPOSIT, SAVINGS, LOAN
    private Long productId;
    private RiskLevel riskLevel; // LOW, MEDIUM, HIGH
    private String riskReason;
    private LocalDateTime evaluatedAt;
    
    // 조회용 추가 필드들
    private String productName;
    private String companyName;
    private String dclsMonth;
    private String finCoNo;
    private String finPrdtCd;
    private LocalDateTime analyzedAt; // evaluatedAt의 별칭
}


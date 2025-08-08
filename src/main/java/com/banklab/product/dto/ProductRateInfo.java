package com.banklab.product.dto;

import com.banklab.product.domain.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 금리 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRateInfo {
    private String dclsMonth;      // 공시월
    private String finCoNo;        // 금융회사번호
    private String finPrdtCd;      // 금융상품코드
    private ProductType productType; // 상품 타입
    private BigDecimal minRate;    // 최저 금리
    private BigDecimal maxRate;    // 최고 금리
    /**
     * 복합 키 생성
     */
    public String getProductKey() {
        return dclsMonth + "_" + finCoNo + "_" + finPrdtCd;
    }
}

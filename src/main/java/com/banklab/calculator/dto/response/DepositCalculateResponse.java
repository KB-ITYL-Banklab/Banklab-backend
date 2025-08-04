package com.banklab.calculator.dto.response;

import com.banklab.calculator.dto.response.common.TaxCalculationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 예금 계산 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositCalculateResponse {
    
    // 입력 조건 (예금 전용)
    private DepositInputConditions inputConditions;
    
    // 계산 결과 (공통 클래스 사용)
    private TaxCalculationResult results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepositInputConditions {
        private Long principal;         // 거치금액
        private Double rate;           // 이자율 (%)
        private Integer termMonths;    // 거치기간 (개월)
        private Boolean isCompound;    // 복리 여부
        private String rateType;       // "복리" 또는 "단리"
    }
}

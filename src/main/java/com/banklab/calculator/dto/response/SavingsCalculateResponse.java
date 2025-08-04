package com.banklab.calculator.dto.response;

import com.banklab.calculator.dto.response.common.TaxCalculationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 적금 계산 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsCalculateResponse {
    
    // 입력 조건 (적금 전용)
    private SavingsInputConditions inputConditions;
    
    // 계산 결과 (공통 클래스 사용)
    private TaxCalculationResult results;
    
    // 적금 전용 결과
    private SavingsSpecificResults savingsSpecific;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SavingsInputConditions {
        private Long monthlyPayment;    // 월 납입금 (목표금액 계산 시 null 가능)
        private Double rate;           // 이자율 (%)
        private Integer termMonths;    // 납입기간 (개월)
        private Long targetAmount;     // 목표 금액 (만기금액 계산 시 null 가능)
        private Boolean isCompound;    // 복리 여부
        private String rateType;       // "복리" 또는 "단리"
        private String calculationType; // "MATURITY_AMOUNT" 또는 "TARGET_AMOUNT"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SavingsSpecificResults {
        private Long totalPayment;                        // 총 납입원금
        private Long requiredMonthlyPaymentGeneral;       // 일반과세 기준 필요 월납입금
        private Long requiredMonthlyPaymentPreferential;  // 세금우대 기준 필요 월납입금  
        private Long requiredMonthlyPaymentExempt;        // 비과세 기준 필요 월납입금
    }
}

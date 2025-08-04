package com.banklab.calculator.dto.response.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 세금 계산 결과 공통 클래스 (예금, 적금 공용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculationResult {
    private Long totalInterest;              // 총 이자
    private TaxCategory generalTax;          // 일반과세
    private TaxCategory preferentialTax;     // 세금우대
    private TaxCategory exemptTax;           // 비과세
    

}

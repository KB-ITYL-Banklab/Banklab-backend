package com.banklab.calculator.util;

import com.banklab.calculator.domain.TaxType;
import com.banklab.calculator.dto.response.common.TaxCalculationResult;
import com.banklab.calculator.dto.response.common.TaxCategory;


/**
 * 세금 계산 공통 유틸리티
 */
public class TaxCalculationUtils {
    
    /**
     * 세금 계산 결과 생성 (예금, 적금 공용)
     * @param principal 원금 (예금) 또는 총 납입금 (적금)
     * @param totalInterest 총 이자
     * @return 세금 계산 결과
     */
    public static TaxCalculationResult calculateTaxResults(Long principal, Long totalInterest) {
        
        // 세금 계산 (이자에 대해서만) - 고객에게 유리하게 내림 처리
        Long taxGeneral = (long) Math.floor(totalInterest * TaxType.GENERAL.getRate());
        Long taxPreferential = (long) Math.floor(totalInterest * TaxType.PREFERENTIAL.getRate());
        Long taxExempt = 0L;
        
        // 세후 이자 계산 (이자에서 세금을 뺀 금액)
        Long afterTaxInterestGeneral = totalInterest - taxGeneral;
        Long afterTaxInterestPreferential = totalInterest - taxPreferential;
        Long afterTaxInterestExempt = totalInterest - taxExempt;
        
        // 만기지급액 계산 (원금 + 세후이자)
        Long maturityAmountGeneral = principal + afterTaxInterestGeneral;
        Long maturityAmountPreferential = principal + afterTaxInterestPreferential;
        Long maturityAmountExempt = principal + afterTaxInterestExempt;
        
        // 세금 카테고리별 결과
        TaxCategory generalTax = TaxCategory.builder()
                .categoryName("일반과세")
                .maturityAmount(maturityAmountGeneral)
                .afterTaxInterest(afterTaxInterestGeneral)
                .taxAmount(taxGeneral)
                .taxRate(TaxType.GENERAL.getRate() * 100) // 퍼센트로 변환
                .build();
        
        TaxCategory preferentialTax =TaxCategory.builder()
                .categoryName("세금우대")
                .maturityAmount(maturityAmountPreferential)
                .afterTaxInterest(afterTaxInterestPreferential)
                .taxAmount(taxPreferential)
                .taxRate(TaxType.PREFERENTIAL.getRate() * 100)
                .build();
        
        TaxCategory exemptTax = TaxCategory.builder()
                .categoryName("비과세")
                .maturityAmount(maturityAmountExempt)
                .afterTaxInterest(afterTaxInterestExempt)
                .taxAmount(taxExempt)
                .taxRate(0.0)
                .build();
        
        return TaxCalculationResult.builder()
                .totalInterest(totalInterest)
                .generalTax(generalTax)
                .preferentialTax(preferentialTax)
                .exemptTax(exemptTax)
                .build();
    }
}

package com.banklab.calculator.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCategory {
    private String categoryName;        // 구분명 (일반과세, 세금우대, 비과세)
    private Long maturityAmount;        // 만기지급액
    private Long afterTaxInterest;      // 세후이자
    private Long taxAmount;             // 세금
    private Double taxRate;             // 세율 (%)
}
package com.banklab.product.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsOption {
    private Long id;
    private String dclsMonth; // 공시 월
    private String finCoNo; // 금융회사 번호  
    private String finPrdtCd; // 금융상품 코드
    private String intrRateType; // 저축금리유형 코드
    private String intrRateTypeNm; // 저축금리유형명
    private String rsrvType; // 적립유형 코드
    private String rsrvTypeNm; // 적립유형명
    private Integer saveTrm; // 저축기간 (개월)
    private BigDecimal intrRate; // 저축금리
    private BigDecimal intrRate2; // 최고우대금리
}

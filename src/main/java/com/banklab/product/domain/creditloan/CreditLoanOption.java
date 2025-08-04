package com.banklab.product.domain.creditloan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLoanOption {
    private Long id;
    private String dclsMonth; // 공시 월
    private String finCoNo; // 금융회사 번호
    private String finPrdtCd; // 금융상품 코드
    private String crdtPrdtType; // 대출상품구분
    private String crdtLendRateType; // 금리구분 코드
    private String crdtLendRateTypeNm; // 금리구분명
    private BigDecimal crdtGrad1; // 신용등급 1등급 금리
    private BigDecimal crdtGrad4; // 신용등급 4등급 금리
    private BigDecimal crdtGrad5; // 신용등급 5등급 금리
    private BigDecimal crdtGrad6; // 신용등급 6등급 금리
    private BigDecimal crdtGrad10; // 신용등급 10등급 금리
    private BigDecimal crdtGrad11; // 신용등급 11등급 금리
    private BigDecimal crdtGrad12; // 신용등급 12등급 금리
    private BigDecimal crdtGrad13; // 신용등급 13등급 금리
    private BigDecimal crdtGradAvg; // 평균 금리
}

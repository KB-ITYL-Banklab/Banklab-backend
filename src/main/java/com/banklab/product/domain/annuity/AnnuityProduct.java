package com.banklab.product.domain.annuity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnuityProduct {
    private Long id;
    private String dclsMonth;           // 공시 제출월
    private String finCoNo;             // 금융회사 코드
    private String finPrdtCd;           // 금융상품 코드
    private String korCoNm;             // 금융회사명
    private String finPrdtNm;           // 금융상품명
    private String joinWay;             // 가입 방법
    private String pnsnKind;            // 연금 종류 코드
    private String pnsnKindNm;          // 연금 종류명
    private String saleStrtDay;         // 판매 시작일
    private Long mntnCnt;               // 유지 개월 수
    private String prdtType;            // 상품 유형 코드
    private String prdtTypeNm;          // 상품 유형명
    private BigDecimal avgPrftRate;     // 평균 수익률
    private BigDecimal dclsRate;        // 공시 수익률
    private BigDecimal guarRate;        // 보장 수익률
    private BigDecimal btrmPrftRate1;   // 1개월 수익률
    private BigDecimal btrmPrftRate2;   // 3개월 수익률
    private BigDecimal btrmPrftRate3;   // 6개월 수익률
    private String etc;                 // 기타사항
    private String saleCo;              // 판매회사
    private LocalDate dclsStrtDay;      // 공시 시작일
    private LocalDate dclsEndDay;       // 공시 종료일
    private LocalDateTime finCoSubmDay; // 금융회사 제출일
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정일시
}
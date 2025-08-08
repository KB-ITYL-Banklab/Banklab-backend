package com.banklab.product.dto.annuity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnuityWithOptionsDto {
    // 상품 기본 정보
    private String dclsMonth;       // 공시월
    private String finCoNo;         // 금융회사번호
    private String finPrdtCd;       // 금융상품코드
    private String finPrdtNm;       // 금융상품명
    private String korCoNm;         // 금융회사명
    private String joinWay;         // 가입방법

    private String pnsnKind;        // 연금저축 종류 코드
    private String pnsnKindNm;      // 연금저축 종류명
    private String prdtType;        // 상품유형 코드
    private String prdtTypeNm;      // 상품유형명

    private String saleStrtDay; // 판매 시작일
    private String saleCo;          // 판매처 목록 (CSV 형태)
    private String dclsStrtDay;  // 공시시작일
    private String dclsEndDay;   // 공시종료일

    // 수익률 정보
    private BigDecimal avgPrftRate;       // 평균 수익률
    private BigDecimal btrmPrftRate1;     // 최근 1년 수익률
    private BigDecimal btrmPrftRate2;     // 최근 3년 수익률
    private BigDecimal btrmPrftRate3;     // 최근 5년 수익률

    // 요약 정보
    private int optionCount;              // 옵션 개수
    private BigDecimal minRecpAmt;        // 최소 연금 수령액
    private BigDecimal maxRecpAmt;        // 최대 연금 수령액

    // 옵션 목록
    private List<AnnuityOptionDto> options;
}

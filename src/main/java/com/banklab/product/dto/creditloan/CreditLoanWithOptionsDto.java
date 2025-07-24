package com.banklab.product.dto.creditloan;

import com.banklab.product.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 신용대출 상품과 옵션 통합 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditLoanWithOptionsDto {
    // 상품 기본 정보
    private String dclsMonth;      // 공시월
    private String finCoNo;        // 금융회사번호
    private String finPrdtCd;      // 금융상품코드
    private String finPrdtNm;      // 금융상품명
    private String korCoNm;        // 금융회사명
    private ProductType productType; // 상품 타입 (항상 CREDIT_LOAN)
    
    // 신용대출 전용 상품 정보
    private String joinWay;        // 가입방법
    private String crdtPrdtType;   // 대출상품구분
    private String crdtPrdtTypeNm; // 대출상품구분명
    private String cbName;         // CB 회사명
    private String spclCnd;        // 특별조건
    private String etcNote;        // 기타사항

    private String dclsStrtDay; // 공시시작일

    private String dclsEndDay;  // 공시종료일
    
    // 신용대출 옵션 목록
    private List<CreditLoanOptionDto> options;
    
    // 요약 정보
    private int optionCount;       // 옵션 개수
    private BigDecimal minRate;    // 최저 평균금리
    private BigDecimal maxRate;    // 최고 평균금리
}

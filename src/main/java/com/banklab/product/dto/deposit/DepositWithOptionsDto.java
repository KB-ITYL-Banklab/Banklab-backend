package com.banklab.product.dto.deposit;

import com.banklab.product.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 예금 상품과 옵션 통합 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepositWithOptionsDto {
    // 상품 기본 정보
    private String dclsMonth;      // 공시월
    private String finCoNo;        // 금융회사번호
    private String finPrdtCd;      // 금융상품코드
    private String finPrdtNm;      // 금융상품명
    private String korCoNm;        // 금융회사명
    private ProductType productType; // 상품 타입 (항상 DEPOSIT)
    
    // 예금 전용 상품 정보
    private String joinWay;        // 가입방법
    private String mtrtInt;        // 만기후이자율
    private String spclCnd;        // 특별조건
    private Integer joinDeny;      // 가입제한
    private String joinMember;     // 가입대상
    private String etcNote;        // 기타사항
    private BigDecimal maxLimit;   // 최고한도
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dclsStrtDay; // 공시시작일
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dclsEndDay;  // 공시종료일
    
    // 예금 옵션 목록
    private List<DepositOptionDto> options;
    
    // 요약 정보
    private int optionCount;       // 옵션 개수
    private BigDecimal minRate;    // 최저 금리
    private BigDecimal maxRate;    // 최고 금리
}

package com.banklab.product.domain.mortgage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MortgageLoanOption {
    
    private Long id;
    private String dclsMonth; // 공시 월
    private String finCoNo; // 금융회사 번호
    private String finPrdtCd; // 금융상품 코드
    private String mrtgType; // 담보유형
    private String mrtgTypeNm; // 담보유형명
    private String rpayType; // 상환유형
    private String rpayTypeNm; // 상환유형명
    private String lendRateType; // 금리유형
    private String lendRateTypeNm; // 금리유형명
    private BigDecimal lendRateMin; // 최저 금리
    private BigDecimal lendRateMax; // 최고 금리
    private BigDecimal lendRateAvg; // 평균 금리
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
}

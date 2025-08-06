package com.banklab.product.domain.renthouse;

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
public class RentHouseLoanOption {
    private Long id;

    private String dclsMonth; // 공시 월 (YYYYMM)

    private String finCoNo; // 금융회사 번호

    private String finPrdtCd; // 금융상품 코드

    private String rpayType; // 상환유형 ("D": 분할상환방식, "S": 만기일시상환방식)

    private String rpayTypeNm; // 상환유형명

    private String lendRateType; // 금리유형 ("F": 고정금리, "C": 변동금리)

    private String lendRateTypeNm; // 금리유형명

    private BigDecimal lendRateMin; // 최저 금리

    private BigDecimal lendRateMax; // 최고 금리

    private BigDecimal lendRateAvg; // 평균 금리

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}

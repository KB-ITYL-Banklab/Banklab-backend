package com.banklab.product.dto.renthouse;

import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentHouseLoanOptionDto {

    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth; // 공시 월

    @JsonProperty("fin_co_no")
    private String finCoNo; // 금융회사 번호

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd; // 금융상품 코드

    @JsonProperty("rpay_type")
    private String rpayType; // 상환유형 ("D": 분할상환방식, "S": 만기일시상환방식)
    
    @JsonProperty("rpay_type_nm")
    private String rpayTypeNm; // 상환유형명

    @JsonProperty("lend_rate_type")
    private String lendRateType; // 금리유형 ("F": 고정금리, "C": 변동금리)
    
    @JsonProperty("lend_rate_type_nm")
    private String lendRateTypeNm; // 금리유형명

    @JsonProperty("lend_rate_min")
    private BigDecimal lendRateMin; // 최저 금리

    @JsonProperty("lend_rate_max")
    private BigDecimal lendRateMax; // 최고 금리

    @JsonProperty("lend_rate_avg")
    private BigDecimal lendRateAvg; // 평균 금리

    public static RentHouseLoanOption toRentHouseLoanOption(RentHouseLoanOptionDto dto) {
        return RentHouseLoanOption.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .finPrdtCd(dto.getFinPrdtCd())
                .rpayType(dto.getRpayType())
                .rpayTypeNm(dto.getRpayTypeNm())
                .lendRateType(dto.getLendRateType())
                .lendRateTypeNm(dto.getLendRateTypeNm())
                .lendRateMin(dto.getLendRateMin())
                .lendRateMax(dto.getLendRateMax())
                .lendRateAvg(dto.getLendRateAvg())
                .build();
    }
}

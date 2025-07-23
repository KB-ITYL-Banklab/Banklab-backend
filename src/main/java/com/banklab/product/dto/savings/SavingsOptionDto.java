package com.banklab.product.dto.savings;

import com.banklab.product.domain.SavingsOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsOptionDto {

    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth; // 공시 월

    @JsonProperty("fin_co_no")
    private String finCoNo; // 금융회사 번호

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd; // 금융상품 코드

    @JsonProperty("intr_rate_type")
    private String intrRateType; // 저축금리유형 코드

    @JsonProperty("intr_rate_type_nm")
    private String intrRateTypeNm; // 저축금리유형명

    @JsonProperty("rsrv_type")
    private String rsrvType; // 적립유형 코드

    @JsonProperty("rsrv_type_nm")
    private String rsrvTypeNm; // 적립유형명

    @JsonProperty("save_trm")
    private String saveTrm; // 저축기간 (문자열)

    @JsonProperty("intr_rate")
    private BigDecimal intrRate; // 저축금리

    @JsonProperty("intr_rate2")
    private BigDecimal intrRate2; // 최고우대금리

    public static SavingsOption toSavingsOption(SavingsOptionDto dto) {
        return SavingsOption.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .finPrdtCd(dto.getFinPrdtCd())
                .intrRateType(dto.getIntrRateType())
                .intrRateTypeNm(dto.getIntrRateTypeNm())
                .rsrvType(dto.getRsrvType())
                .rsrvTypeNm(dto.getRsrvTypeNm())
                .saveTrm(dto.getSaveTrm() != null ? Integer.parseInt(dto.getSaveTrm()) : null)
                .intrRate(dto.getIntrRate())
                .intrRate2(dto.getIntrRate2())
                .build();
    }
}
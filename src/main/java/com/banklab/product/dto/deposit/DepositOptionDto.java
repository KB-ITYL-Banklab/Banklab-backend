package com.banklab.product.dto.deposit;

import com.banklab.product.domain.DepositOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositOptionDto {
    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth;

    @JsonProperty("fin_co_no")
    private String finCoNo;

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("intr_rate_type")
    private String intrRateType;

    @JsonProperty("intr_rate_type_nm")
    private String intrRateTypeNm;

    @JsonProperty("save_trm")
    private String saveTrmStr; // API에서는 String으로 옴 (예: "1", "3", "6")

    @JsonProperty("intr_rate")
    private BigDecimal intrRate;

    @JsonProperty("intr_rate2")
    private BigDecimal intrRate2;


    public static DepositOption toDepositOption(DepositOptionDto dto) {
        return DepositOption.builder()
                .id(dto.getId())
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .finPrdtCd(dto.getFinPrdtCd())
                .intrRateType(dto.getIntrRateType())
                .intrRateTypeNm(dto.getIntrRateTypeNm())
                .saveTrm(parseSaveTrm(dto.getSaveTrmStr()))
                .intrRate(dto.getIntrRate())
                .intrRate2(dto.getIntrRate2())
                .build();
    }

    private static Integer parseSaveTrm(String saveTrmStr) {
        try {
            return saveTrmStr != null ? Integer.parseInt(saveTrmStr) : 12;
        } catch (NumberFormatException e) {
            return 12;
        }
    }
}

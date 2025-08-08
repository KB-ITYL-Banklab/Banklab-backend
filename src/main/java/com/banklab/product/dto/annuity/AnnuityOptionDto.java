package com.banklab.product.dto.annuity;

import com.banklab.product.domain.annuity.AnnuityOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnuityOptionDto {
    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth;
    @JsonProperty("fin_co_no")
    private String finCoNo;
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("pnsn_recp_trm")
    private String pnsnRecpTrm;
    @JsonProperty("pnsn_recp_trm_nm")
    private String pnsnRecpTrmNm;

    @JsonProperty("pnsn_entr_age")
    private String pnsnEntrAge;
    @JsonProperty("pnsn_entr_age_nm")
    private String pnsnEntrAgeNm;

    @JsonProperty("mon_paym_atm")
    private String monPaymAtm;
    @JsonProperty("mon_paym_atm_nm")
    private String monPaymAtmNm;

    @JsonProperty("paym_prd")
    private String paymPrd;
    @JsonProperty("paym_prd_nm")
    private String paymPrdNm;

    @JsonProperty("pnsn_strt_age")
    private String pnsnStrtAge;
    @JsonProperty("pnsn_strt_age_nm")
    private String pnsnStrtAgeNm;

    @JsonProperty("pnsn_recp_amt")
    private BigDecimal pnsnRecpAmt;

    public static AnnuityOption toAnnuityOption(AnnuityOptionDto optionDto) {
        return AnnuityOption.builder()
                .id(optionDto.getId())
                .dclsMonth(optionDto.getDclsMonth())
                .finCoNo(optionDto.getFinCoNo())
                .finPrdtCd(optionDto.getFinPrdtCd())
                .pnsnRecpTrm(optionDto.getPnsnRecpTrm())
                .pnsnRecpTrmNm(optionDto.getPnsnRecpTrmNm())
                .pnsnEntrAge(optionDto.getPnsnEntrAge())
                .pnsnEntrAgeNm(optionDto.getPnsnEntrAgeNm())
                .monPaymAtm(optionDto.getMonPaymAtm())
                .monPaymAtmNm(optionDto.getMonPaymAtmNm())
                .paymPrd(optionDto.getPaymPrd())
                .paymPrdNm(optionDto.getPaymPrdNm())
                .pnsnStrtAge(optionDto.getPnsnStrtAge())
                .pnsnStrtAgeNm(optionDto.getPnsnStrtAgeNm())
                .pnsnRecpAmt(optionDto.getPnsnRecpAmt())
                .build();
    }
}

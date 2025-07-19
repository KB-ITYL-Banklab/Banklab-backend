package com.banklab.product.dto.deposit;


import com.banklab.product.domain.DepositProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositProductDto {
    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth;

    @JsonProperty("fin_co_no")
    private String finCoNo;

    @JsonProperty("kor_co_nm")
    private String korCoNm;

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("fin_prdt_nm")
    private String finPrdtNm;

    @JsonProperty("join_way")
    private String joinWay;

    @JsonProperty("mtrt_int")
    private String mtrtInt;

    @JsonProperty("spcl_cnd")
    private String spclCnd;

    @JsonProperty("join_deny")
    private String joinDeny;

    @JsonProperty("join_member")
    private String joinMember;

    @JsonProperty("etc_note")
    private String etcNote;

    @JsonProperty("max_limit")
    private BigDecimal maxLimit;

    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay;

    @JsonProperty("dcls_end_day")
    private String dclsEndDay;

    @JsonProperty("fin_co_subm_day")
    private String finCoSubmDayr;

    public static DepositProduct toDepositProduct(DepositProductDto dto) {
        return DepositProduct.builder()
                .id(dto.getId())
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .korCoNm(dto.getKorCoNm())
                .finPrdtCd(dto.getFinPrdtCd())
                .finPrdtNm(dto.getFinPrdtNm())
                .joinWay(dto.getJoinWay())
                .mtrtInt(dto.getMtrtInt())
                .spclCnd(dto.getSpclCnd())
                .joinDeny(parseJoinDeny(dto.getJoinDeny()))
                .joinMember(dto.getJoinMember())
                .etcNote(dto.getEtcNote())
                .maxLimit(dto.getMaxLimit())
                .dclsStrtDay(parseDate(dto.getDclsStrtDay()))
                .dclsEndDay(parseDate(dto.getDclsEndDay()))
                .finCoSubmDay(parseDateTime(dto.getFinCoSubmDayr()))
                .build();
    }

    private static Integer parseJoinDeny(String joinDenyStr) {
        try {
            return joinDenyStr != null ? Integer.parseInt(joinDenyStr) : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equals(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || "null".equals(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        } catch (Exception e) {
            return null;
        }
    }

}


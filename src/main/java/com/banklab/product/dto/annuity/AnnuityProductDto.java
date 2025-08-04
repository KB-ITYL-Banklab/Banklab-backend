package com.banklab.product.dto.annuity;

import com.banklab.product.domain.annuity.AnnuityProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnuityProductDto {
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
    @JsonProperty("pnsn_kind")
    private String pnsnKind;
    @JsonProperty("pnsn_kind_nm")
    private String pnsnKindNm;
    @JsonProperty("sale_strt_day")
    private String saleStrtDay;
    @JsonProperty("mntn_cnt")
    private String mntnCnt;
    @JsonProperty("prdt_type")
    private String prdtType;
    @JsonProperty("prdt_type_nm")
    private String prdtTypeNm;
    @JsonProperty("avg_prft_rate")
    private String avgPrftRate;
    @JsonProperty("dcls_rate")
    private String dclsRate;
    @JsonProperty("guar_rate")
    private String guarRate;
    @JsonProperty("btrm_prft_rate_1")
    private String btrmPrftRate1;
    @JsonProperty("btrm_prft_rate_2")
    private String btrmPrftRate2;
    @JsonProperty("btrm_prft_rate_3")
    private String btrmPrftRate3;
    @JsonProperty("etc")
    private String etc;
    @JsonProperty("sale_co")
    private String saleCo;
    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay;
    @JsonProperty("dcls_end_day")
    private String dclsEndDay;
    @JsonProperty("fin_co_subm_day")
    private String finDoSubmDay;

    public static AnnuityProduct toAnnuityProduct(AnnuityProductDto baseDto) {
        return AnnuityProduct.builder()
                .dclsMonth(baseDto.getDclsMonth())
                .finCoNo(baseDto.getFinCoNo())
                .korCoNm(baseDto.getKorCoNm())
                .finPrdtCd(baseDto.getFinPrdtCd())
                .finPrdtNm(baseDto.getFinPrdtNm())
                .joinWay(baseDto.getJoinWay())
                .pnsnKind(baseDto.getPnsnKind())
                .pnsnKindNm(baseDto.getPnsnKindNm())
                .saleStrtDay(baseDto.getSaleStrtDay())
                .mntnCnt(parseLong(baseDto.getMntnCnt()))
                .prdtType(baseDto.getPrdtType())
                .prdtTypeNm(baseDto.getPrdtTypeNm())
                .avgPrftRate(parseBigDecimal(baseDto.getAvgPrftRate()))
                .dclsRate(parseBigDecimal(baseDto.getDclsRate()))
                .guarRate(parseBigDecimal(baseDto.getGuarRate()))
                .btrmPrftRate1(parseBigDecimal(baseDto.getBtrmPrftRate1()))
                .btrmPrftRate2(parseBigDecimal(baseDto.getBtrmPrftRate2()))
                .btrmPrftRate3(parseBigDecimal(baseDto.getBtrmPrftRate3()))
                .etc(baseDto.getEtc())
                .saleCo(baseDto.getSaleCo())
                .dclsStrtDay(parseDate(baseDto.getDclsStrtDay()))
                .dclsEndDay(parseDate(baseDto.getDclsEndDay()))
                .finCoSubmDay(parseDateTime(baseDto.getFinDoSubmDay()))
                .build();
    }

    private static Long parseLong(String value) {
        return value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value)
                ? Long.valueOf(value)
                : null;
    }

    private static BigDecimal parseBigDecimal(String value) {
        return value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value)
                ? new BigDecimal(value)
                : null;
    }

    private static LocalDate parseDate(String dateStr) {
        return dateStr != null && !dateStr.trim().isEmpty() && !"null".equalsIgnoreCase(dateStr)
                ? LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"))
                : null;
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || "null".equalsIgnoreCase(dateTimeStr)) {
            return null;
        }
        if (dateTimeStr.length() == 12) {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        } else if (dateTimeStr.length() == 8) {
            return LocalDate.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
        }
        return LocalDateTime.parse(dateTimeStr);
    }
}

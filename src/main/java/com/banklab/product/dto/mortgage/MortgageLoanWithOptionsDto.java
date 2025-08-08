package com.banklab.product.dto.mortgage;

import com.banklab.product.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MortgageLoanWithOptionsDto {
    // 상품 기본 정보
    private String dclsMonth;
    private String finCoNo;
    private String finPrdtCd;
    private String finPrdtNm;
    private String korCoNm;
    private ProductType productType;

    private String joinWay;
    private String loanInciExpn;
    private String erlyRpayFee;
    private String dlyRate;
    private String loanLmt;
    private String dclsStrtDay;
    private String dclsEndDay;

    // 옵션 목록
    private List<MortgageLoanOptionDto> options;

    // 요약 정보
    private int optionCount;
    private BigDecimal minRate; // 옵션 중 최저 평균금리 (lendRateAvg)
    private BigDecimal maxRate; // 옵션 중 최고 평균금리 (lendRateAvg)
}

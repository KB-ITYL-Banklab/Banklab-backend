package com.banklab.product.dto.creditloan;

import com.banklab.product.domain.CreditLoanOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreditLoanOptionDto {

    private Long id;

    @JsonProperty("dcls_month")
    private String dclsMonth; // 공시 월

    @JsonProperty("fin_co_no")
    private String finCoNo; // 금융회사 번호

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd; // 금융상품 코드

    @JsonProperty("crdt_prdt_type")
    private String crdtPrdtType; // 대출상품구분

    @JsonProperty("crdt_lend_rate_type")
    private String crdtLendRateType; // 금리구분

    @JsonProperty("crdt_lend_rate_type_nm")
    private String crdtLendRateTypeNm; // 금리구분명

    @JsonProperty("crdt_grad_1")
    private BigDecimal crdtGrad1; // 신용등급 1등급 금리

    @JsonProperty("crdt_grad_4")
    private BigDecimal crdtGrad4; // 신용등급 4등급 금리

    @JsonProperty("crdt_grad_5")
    private BigDecimal crdtGrad5; // 신용등급 5등급 금리

    @JsonProperty("crdt_grad_6")
    private BigDecimal crdtGrad6; // 신용등급 6등급 금리

    @JsonProperty("crdt_grad_10")
    private BigDecimal crdtGrad10; // 신용등급 10등급 금리

    @JsonProperty("crdt_grad_11")
    private BigDecimal crdtGrad11; // 신용등급 11등급 금리

    @JsonProperty("crdt_grad_12")
    private BigDecimal crdtGrad12; // 신용등급 12등급 금리

    @JsonProperty("crdt_grad_13")
    private BigDecimal crdtGrad13; // 신용등급 13등급 금리

    @JsonProperty("crdt_grad_avg")
    private BigDecimal crdtGradAvg; // 평균 금리

    public static CreditLoanOption toCreditLoanOption(CreditLoanOptionDto dto) {
        return CreditLoanOption.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .finPrdtCd(dto.getFinPrdtCd())
                .crdtPrdtType(dto.getCrdtPrdtType())
                .crdtLendRateType(dto.getCrdtLendRateType())
                .crdtLendRateTypeNm(dto.getCrdtLendRateTypeNm())
                .crdtGrad1(dto.getCrdtGrad1())
                .crdtGrad4(dto.getCrdtGrad4())
                .crdtGrad5(dto.getCrdtGrad5())
                .crdtGrad6(dto.getCrdtGrad6())
                .crdtGrad10(dto.getCrdtGrad10())
                .crdtGrad11(dto.getCrdtGrad11())
                .crdtGrad12(dto.getCrdtGrad12())
                .crdtGrad13(dto.getCrdtGrad13())
                .crdtGradAvg(dto.getCrdtGradAvg())
                .build();
    }
}
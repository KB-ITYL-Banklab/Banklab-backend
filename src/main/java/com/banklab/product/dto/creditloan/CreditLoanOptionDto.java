package com.banklab.product.dto.creditloan;

import com.banklab.product.domain.CreditLoanOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditLoanOptionDto {
    
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
    private Double crdtGrad1; // 신용등급 1등급 금리
    
    @JsonProperty("crdt_grad_4")
    private Double crdtGrad4; // 신용등급 4등급 금리
    
    @JsonProperty("crdt_grad_5")
    private Double crdtGrad5; // 신용등급 5등급 금리
    
    @JsonProperty("crdt_grad_6")
    private Double crdtGrad6; // 신용등급 6등급 금리
    
    @JsonProperty("crdt_grad_10")
    private Double crdtGrad10; // 신용등급 10등급 금리
    
    @JsonProperty("crdt_grad_11")
    private Double crdtGrad11; // 신용등급 11등급 금리
    
    @JsonProperty("crdt_grad_12")
    private Double crdtGrad12; // 신용등급 12등급 금리
    
    @JsonProperty("crdt_grad_13")
    private Double crdtGrad13; // 신용등급 13등급 금리
    
    @JsonProperty("crdt_grad_avg")
    private Double crdtGradAvg; // 평균 금리

    public static CreditLoanOption toCreditLoanOption(CreditLoanOptionDto dto) {
        return CreditLoanOption.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .finPrdtCd(dto.getFinPrdtCd())
                .crdtPrdtType(dto.getCrdtPrdtType())
                .crdtLendRateType(dto.getCrdtLendRateType())
                .crdtLendRateTypeNm(dto.getCrdtLendRateTypeNm())
                .crdtGrad1(dto.getCrdtGrad1() != null ? new BigDecimal(dto.getCrdtGrad1().toString()) : null)
                .crdtGrad4(dto.getCrdtGrad4() != null ? new BigDecimal(dto.getCrdtGrad4().toString()) : null)
                .crdtGrad5(dto.getCrdtGrad5() != null ? new BigDecimal(dto.getCrdtGrad5().toString()) : null)
                .crdtGrad6(dto.getCrdtGrad6() != null ? new BigDecimal(dto.getCrdtGrad6().toString()) : null)
                .crdtGrad10(dto.getCrdtGrad10() != null ? new BigDecimal(dto.getCrdtGrad10().toString()) : null)
                .crdtGrad11(dto.getCrdtGrad11() != null ? new BigDecimal(dto.getCrdtGrad11().toString()) : null)
                .crdtGrad12(dto.getCrdtGrad12() != null ? new BigDecimal(dto.getCrdtGrad12().toString()) : null)
                .crdtGrad13(dto.getCrdtGrad13() != null ? new BigDecimal(dto.getCrdtGrad13().toString()) : null)
                .crdtGradAvg(dto.getCrdtGradAvg() != null ? new BigDecimal(dto.getCrdtGradAvg().toString()) : null)
                .build();
    }
}

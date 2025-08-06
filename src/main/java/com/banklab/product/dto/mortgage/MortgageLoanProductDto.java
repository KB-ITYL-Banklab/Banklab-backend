package com.banklab.product.dto.mortgage;

import com.banklab.product.domain.mortgage.MortgageLoanProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MortgageLoanProductDto {
    
    private Long id;
    
    @JsonProperty("dcls_month")
    private String dclsMonth; // 공시 월
    
    @JsonProperty("fin_co_no")
    private String finCoNo; // 금융회사 번호
    
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd; // 금융상품 코드
    
    @JsonProperty("kor_co_nm")
    private String korCoNm; // 금융회사명
    
    @JsonProperty("fin_prdt_nm")
    private String finPrdtNm; // 금융상품명
    
    @JsonProperty("join_way")
    private String joinWay; // 가입방법
    
    @JsonProperty("loan_inci_expn")
    private String loanInciExpn; // 대출부대비용
    
    @JsonProperty("erly_rpay_fee")
    private String erlyRpayFee; // 중도상환수수료
    
    @JsonProperty("dly_rate")
    private String dlyRate; // 연체이자율
    
    @JsonProperty("loan_lmt")
    private String loanLmt; // 대출한도
    
    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay; // 공시 시작일
    
    @JsonProperty("dcls_end_day")
    private String dclsEndDay; // 공시 종료일
    
    @JsonProperty("fin_co_subm_day")
    private String finCoSubmDay; // 금융회사 제출일

    public static MortgageLoanProduct toMortgageLoanProduct(MortgageLoanProductDto dto) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        
        return MortgageLoanProduct.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .korCoNm(dto.getKorCoNm())
                .finPrdtCd(dto.getFinPrdtCd())
                .finPrdtNm(dto.getFinPrdtNm())
                .joinWay(dto.getJoinWay())
                .loanInciExpn(dto.getLoanInciExpn())
                .erlyRpayFee(dto.getErlyRpayFee())
                .dlyRate(dto.getDlyRate())
                .loanLmt(dto.getLoanLmt())
                .dclsStrtDay(dto.getDclsStrtDay() != null ? LocalDate.parse(dto.getDclsStrtDay(), dateFormatter) : null)
                .dclsEndDay(dto.getDclsEndDay() != null ? LocalDate.parse(dto.getDclsEndDay(), dateFormatter) : null)
                .finCoSubmDay(dto.getFinCoSubmDay() != null ? LocalDateTime.parse(dto.getFinCoSubmDay(), dateTimeFormatter) : null)
                .build();
    }
}

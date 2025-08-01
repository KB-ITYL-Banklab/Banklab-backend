package com.banklab.product.dto.savings;

import com.banklab.product.domain.SavingsProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsProductDto {
    
    private Long id; // 데이터베이스 ID
    
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
    
    @JsonProperty("mtrt_int")
    private String mtrtInt; // 만기후이자율
    
    @JsonProperty("spcl_cnd")
    private String spclCnd; // 우대조건
    
    @JsonProperty("join_deny")
    private String joinDeny; // 가입제한
    
    @JsonProperty("join_member")
    private String joinMember; // 가입대상
    
    @JsonProperty("etc_note")
    private String etcNote; // 기타유의사항
    
    @JsonProperty("max_limit")
    private String maxLimit; // 최고한도
    
    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay; // 공시 시작일
    
    @JsonProperty("dcls_end_day")
    private String dclsEndDay; // 공시 종료일
    
    @JsonProperty("fin_co_subm_day")
    private String finCoSubmDay; // 금융회사 제출일


    public static SavingsProduct toSavingsProduct(SavingsProductDto dto) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        
        return SavingsProduct.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .korCoNm(dto.getKorCoNm())
                .finPrdtCd(dto.getFinPrdtCd())
                .finPrdtNm(dto.getFinPrdtNm())
                .joinWay(dto.getJoinWay())
                .mtrtInt(dto.getMtrtInt())
                .spclCnd(dto.getSpclCnd())
                .joinDeny(dto.getJoinDeny() != null ? Integer.parseInt(dto.getJoinDeny()) : null)
                .joinMember(dto.getJoinMember())
                .etcNote(dto.getEtcNote())
                .maxLimit(dto.getMaxLimit() != null ? new BigDecimal(dto.getMaxLimit()) : null)
                .dclsStrtDay(dto.getDclsStrtDay() != null ? LocalDate.parse(dto.getDclsStrtDay(), dateFormatter) : null)
                .dclsEndDay(dto.getDclsEndDay() != null ? LocalDate.parse(dto.getDclsEndDay(), dateFormatter) : null)
                .finCoSubmDay(dto.getFinCoSubmDay() != null ? LocalDateTime.parse(dto.getFinCoSubmDay(), dateTimeFormatter) : null)
                .build();
    }
}

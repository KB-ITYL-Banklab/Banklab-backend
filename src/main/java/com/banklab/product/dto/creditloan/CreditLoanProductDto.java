package com.banklab.product.dto.creditloan;

import com.banklab.product.domain.creditloan.CreditLoanProduct;
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
public class CreditLoanProductDto {
    
    private Long id;
    
    @JsonProperty("dcls_month")
    private String dclsMonth; // 공시 월
    
    @JsonProperty("fin_co_no")
    private String finCoNo; // 금융회사 번호
    
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd; // 금융상품 코드
    
    @JsonProperty("crdt_prdt_type")
    private String crdtPrdtType; // 대출상품구분
    
    @JsonProperty("kor_co_nm")
    private String korCoNm; // 금융회사명
    
    @JsonProperty("fin_prdt_nm")
    private String finPrdtNm; // 금융상품명
    
    @JsonProperty("join_way")
    private String joinWay; // 가입방법
    
    @JsonProperty("cb_name")
    private String cbName; // 신용평가회사
    
    @JsonProperty("crdt_prdt_type_nm")
    private String crdtPrdtTypeNm; // 대출상품구분명
    
    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay; // 공시 시작일
    
    @JsonProperty("dcls_end_day")
    private String dclsEndDay; // 공시 종료일
    
    @JsonProperty("fin_co_subm_day")
    private String finCoSubmDay; // 금융회사 제출일
    
    // 위험도 분석용 추가 필드들
    private String spclCnd; // 우대조건 (실제 API에는 없지만 분석용)
    private String etcNote; // 기타유의사항 (실제 API에는 없지만 분석용)

    public static CreditLoanProduct toCreditLoanProduct(CreditLoanProductDto dto) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        
        return CreditLoanProduct.builder()
                .dclsMonth(dto.getDclsMonth())
                .finCoNo(dto.getFinCoNo())
                .korCoNm(dto.getKorCoNm())
                .finPrdtCd(dto.getFinPrdtCd())
                .finPrdtNm(dto.getFinPrdtNm())
                .joinWay(dto.getJoinWay())
                .crdtPrdtType(dto.getCrdtPrdtType())
                .crdtPrdtTypeNm(dto.getCrdtPrdtTypeNm())
                .cbName(dto.getCbName())
                .dclsStrtDay(dto.getDclsStrtDay() != null ? LocalDate.parse(dto.getDclsStrtDay(), dateFormatter) : null)
                .dclsEndDay(dto.getDclsEndDay() != null ? LocalDate.parse(dto.getDclsEndDay(), dateFormatter) : null)
                .finCoSubmDay(dto.getFinCoSubmDay() != null ? LocalDateTime.parse(dto.getFinCoSubmDay(), dateTimeFormatter) : null)
                .build();
    }
}

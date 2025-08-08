package com.banklab.product.dto.renthouse;

import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
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
public class RentHouseLoanProductDto {
    
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

    public static RentHouseLoanProduct toRentHouseLoanProduct(RentHouseLoanProductDto dto) {
        return RentHouseLoanProduct.builder()
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
                .dclsStrtDay(parseDate(dto.getDclsStrtDay()))
                .dclsEndDay(parseDate(dto.getDclsEndDay()))
                .finCoSubmDay(parseDateTime(dto.getFinCoSubmDay()))
                .build();
    }

    private static LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty() || "null".equals(dateStr)) {
                return null;
            }
            
            String cleanDateStr = dateStr.trim();
            
            // yyyy-MM-dd 형태인 경우 (예: 2025-07-20)
            if (cleanDateStr.contains("-") && cleanDateStr.length() == 10) {
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            // yyyyMMdd 형태인 경우 (예: 20250720)
            else if (cleanDateStr.length() == 8) {
                return LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("날짜 파싱 오류: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || "null".equals(dateTimeStr)) {
                return null;
            }
            
            String cleanDateTimeStr = dateTimeStr.trim();
            
            // yyyyMMddHHmm 형태인 경우 (예: 202507181933)
            if (cleanDateTimeStr.length() == 12) {
                return LocalDateTime.parse(cleanDateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            }
            // yyyy-MM-dd HH:mm:ss 형태인 경우
            else if (cleanDateTimeStr.contains("-") && cleanDateTimeStr.contains(":")) {
                if (cleanDateTimeStr.length() == 19) {
                    return LocalDateTime.parse(cleanDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else if (cleanDateTimeStr.length() == 16) {
                    return LocalDateTime.parse(cleanDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("날짜시간 파싱 오류: " + dateTimeStr + " - " + e.getMessage());
            return null;
        }
    }
}

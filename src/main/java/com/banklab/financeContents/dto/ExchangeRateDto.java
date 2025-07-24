package com.banklab.financeContents.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 환율 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {
    
    /**
     * 결과값 (1: 성공, 2: 데이터 코드 오류, 3: 인증코드 오류, 4: 일일제한횟수 마감)
     */
    private String result;
    
    /**
     * 통화코드 (USD, EUR, JPY 등)
     */
    private String cur_unit;
    
    /**
     * 전신환(송금) 받으실때
     */
    private String ttb;
    
    /**
     * 전신환(송금) 보내실때  
     */
    private String tts;
    
    /**
     * 매매기준율
     */
    private String deal_bas_r;
    
    /**
     * 장부가격
     */
    private String bkpr;
    
    /**
     * 년환가료율
     */
    private String yy_efee_r;
    
    /**
     * 10일환가료율
     */
    private String ten_dd_efee_r;
    
    /**
     * 서울외국환중개 매매기준율
     */
    private String kftc_bkpr;
    
    /**
     * 서울외국환중개 장부가격
     */
    private String kftc_deal_bas_r;
    
    /**
     * 통화명
     */
    private String cur_nm;
}

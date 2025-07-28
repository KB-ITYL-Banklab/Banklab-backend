package com.banklab.financeContents.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 개별 금 시세 정보를 담는 Data Transfer Object
 * 
 * 실제 API 응답 구조에 맞춰 수정된 DTO
 * API 응답 예시:
 * {
 *   "basDt": "20250721",
 *   "srtnCd": "04020000",
 *   "isinCd": "KRD040200002",
 *   "itmsNm": "금 99.99_1Kg",
 *   "clpr": "150400",
 *   "vs": "900",
 *   "fltRt": ".6",
 *   "mkp": "149520",
 *   "hipr": "150560",
 *   "lopr": "149520",
 *   "trqu": "265716",
 *   "trPrc": "39907661290"
 * }
 * 
 * @author 개발팀
 * @version 2.0
 * @since 2025.01
 * @see GoldApiResponseDto API 전체 응답 구조
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GoldPriceInfoDto {
    
    /** 기준일자 (YYYYMMDD 형식, 예: "20250721") */
    @JsonProperty("basDt")
    private String baseDate;
    
    /** 단축코드 (예: "04020000") */
    @JsonProperty("srtnCd")
    private String shortCode;
    
    /** ISIN 코드 (국제증권식별번호, 예: "KRD040200002") */
    @JsonProperty("isinCd")
    private String isinCode;
    
    /** 종목명 (예: "금 99.99_1Kg") */
    @JsonProperty("itmsNm")
    private String itemName;
    
    /** 종가 (예: "150400") */
    @JsonProperty("clpr")
    private String closePrice;
    
    /** 대비 (전일 대비 변동폭, 예: "900") */
    @JsonProperty("vs")
    private String versus;
    
    /** 등락률 (예: ".6") */
    @JsonProperty("fltRt")
    private String fluctuationRate;
    
    /** 시가 (시장 개장가, 예: "149520") */
    @JsonProperty("mkp")
    private String marketPrice;
    
    /** 고가 (당일 최고가, 예: "150560") */
    @JsonProperty("hipr")
    private String highPrice;
    
    /** 저가 (당일 최저가, 예: "149520") */
    @JsonProperty("lopr")
    private String lowPrice;
    
    /** 거래량 (예: "265716") */
    @JsonProperty("trqu")
    private String tradingQuantity;
    
    /** 거래대금 (예: "39907661290") */
    @JsonProperty("trPrc")
    private String tradingPrice;
    
    // === 편의 메서드들 ===
    
    /**
     * 상품명을 반환 (호환성을 위해)
     * @return 종목명
     */
    public String getProductName() {
        return this.itemName;
    }
    
    /**
     * 상품코드를 반환 (호환성을 위해)
     * @return ISIN 코드
     */
    public String getProductCode() {
        return this.isinCode;
    }
    
    /**
     * 종가를 숫자로 변환
     * @return 종가 (숫자형), 파싱 실패시 0
     */
    public double getClosePriceAsDouble() {
        try {
            return closePrice != null ? Double.parseDouble(closePrice) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 등락률을 숫자로 변환
     * @return 등락률 (숫자형), 파싱 실패시 0
     */
    public double getFluctuationRateAsDouble() {
        try {
            return fluctuationRate != null ? Double.parseDouble(fluctuationRate) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 거래량을 숫자로 변환
     * @return 거래량 (숫자형), 파싱 실패시 0
     */
    public long getTradingQuantityAsLong() {
        try {
            return tradingQuantity != null ? Long.parseLong(tradingQuantity) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 거래대금을 숫자로 변환
     * @return 거래대금 (숫자형), 파싱 실패시 0
     */
    public long getTradingPriceAsLong() {
        try {
            return tradingPrice != null ? Long.parseLong(tradingPrice) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 포맷된 종가 문자열 반환
     * @return 콤마가 포함된 종가 문자열
     */
    public String getFormattedClosePrice() {
        try {
            if (closePrice != null) {
                long price = Long.parseLong(closePrice);
                return String.format("%,d", price);
            }
        } catch (NumberFormatException e) {
            // 무시
        }
        return closePrice;
    }
    
    /**
     * 포맷된 거래대금 문자열 반환
     * @return 콤마가 포함된 거래대금 문자열
     */
    public String getFormattedTradingPrice() {
        try {
            if (tradingPrice != null) {
                long price = Long.parseLong(tradingPrice);
                return String.format("%,d", price);
            }
        } catch (NumberFormatException e) {
            // 무시
        }
        return tradingPrice;
    }
}
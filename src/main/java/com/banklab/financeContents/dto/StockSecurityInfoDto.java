package com.banklab.financeContents.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 개별 주식 정보를 담는 Data Transfer Object
 * 
 * 이 클래스는 공공데이터포털의 주식시세정보 API에서 제공하는
 * 개별 종목의 상세 정보를 자바 객체로 매핑하기 위한 DTO입니다.
 * 
 * 포함 정보:
 * - 기본 정보: 종목명, 종목코드, 시장구분
 * - 가격 정보: 시가, 고가, 저가, 종가, 등락률
 * - 거래 정보: 거래량, 거래대금
 * - 기타 정보: 상장주수, 시가총액
 * 
 * API에서 제공하는 모든 데이터는 문자열 형태로 전달되므로,
 * 필요시 숫자 변환 로직을 별도로 구현해야 합니다.
 */
@Data
public class StockSecurityInfoDto {
    
    /** 기준일자 (YYYYMMDD 형식, 예: "20250122") */
    @JsonProperty("basDt")
    private String baseDate;
    
    /** 종목 단축코드 (6자리, 예: "005930") */
    @JsonProperty("srtnCd")
    private String shortCode;
    
    /** 국제증권식별번호 (ISIN Code, 예: "KR7005930003") */
    @JsonProperty("isinCd")
    private String isinCode;
    
    /** 종목명 (예: "삼성전자", "SK하이닉스") */
    @JsonProperty("itmsNm")
    private String itemName;
    
    /** 시장구분 ("KOSPI", "KOSDAQ" 등) */
    @JsonProperty("mrktCtg")
    private String marketCategory;
    
    /** 종가 (단위: 원, 문자열 형태로 제공) */
    @JsonProperty("clpr")
    private String closePrice;
    
    /** 전일 대비 가격 변동 (단위: 원, 음수 가능) */
    @JsonProperty("vs")
    private String versus;
    
    /** 등락률 (단위: %, 음수 가능, 예: "1.23", "-2.45") */
    @JsonProperty("fltRt")
    private String fluctuationRate;
    
    /** 시가 (단위: 원, 하루 첫 거래 가격) */
    @JsonProperty("mkp")
    private String marketPrice;
    
    /** 고가 (단위: 원, 하루 중 최고 거래 가격) */
    @JsonProperty("hipr")
    private String highPrice;
    
    /** 저가 (단위: 원, 하루 중 최저 거래 가격) */
    @JsonProperty("lopr")
    private String lowPrice;
    
    /** 거래량 (단위: 주, 하루 총 거래된 주식 수) */
    @JsonProperty("trqu")
    private String tradingQuantity;
    
    /** 거래대금 (단위: 원, 하루 총 거래 금액) */
    @JsonProperty("trPrc")
    private String tradingPrice;
    
    /** 상장주수 (시장에 상장된 총 주식 수) */
    @JsonProperty("lstgStCnt")
    private String listedStockCount;
    
    /** 시가총액 (단위: 원, 상장주수 × 현재가격) */
    @JsonProperty("mrktTotAmt")
    private String marketTotalAmount;
}

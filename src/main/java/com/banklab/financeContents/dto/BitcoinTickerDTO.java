package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 업비트 API 비트코인 시세 정보를 담는 DTO (Data Transfer Object) 클래스
 * 
 * <p>업비트 공개 API에서 제공하는 암호화폐 시세 정보를 Java 객체로 매핑합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>실시간 비트코인 시세 정보 저장</li>
 *   <li>JSON 응답 데이터를 Java 객체로 자동 변환</li>
 *   <li>52주 최고/최저가 정보 포함</li>
 *   <li>24시간 거래량 및 거래대금 정보</li>
 * </ul>
 * 
 * <h3>API 응답 예시:</h3>
 * <pre>
 * {
 *   "market": "KRW-BTC",
 *   "trade_price": 159717000.00000000,
 *   "change": "FALL",
 *   "change_rate": 0.002367296
 * }
 * </pre>
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2025-01-22
 * @see <a href="https://docs.upbit.com/reference/ticker%ED%98%84%EC%9E%AC%EA%B0%80-%EB%82%B4%EC%97%AD">업비트 API 문서</a>
 */
public class BitcoinTickerDTO {
    
    /** 마켓 코드 (예: KRW-BTC) */
    @JsonProperty("market")
    private String market;
    
    /** 최근 거래 일자 (YYYYMMDD 형식) */
    @JsonProperty("trade_date")
    private String tradeDate;
    
    /** 최근 거래 시각 (HHMMSS 형식) */
    @JsonProperty("trade_time")
    private String tradeTime;
    
    /** 최근 거래 일자 한국시간 (YYYYMMDD 형식) */
    @JsonProperty("trade_date_kst")
    private String tradeDateKst;
    
    /** 최근 거래 시각 한국시간 (HHMMSS 형식) */
    @JsonProperty("trade_time_kst")
    private String tradeTimeKst;
    
    /** 최근 거래 일시 Unix timestamp (밀리초) */
    @JsonProperty("trade_timestamp")
    private Long tradeTimestamp;
    
    /** 시가 (KRW) */
    @JsonProperty("opening_price")
    private Double openingPrice;
    
    /** 고가 (KRW) */
    @JsonProperty("high_price")
    private Double highPrice;
    
    /** 저가 (KRW) */
    @JsonProperty("low_price")
    private Double lowPrice;
    
    /** 현재가 (KRW) - 가장 최근 거래된 가격 */
    @JsonProperty("trade_price")
    private Double tradePrice;
    
    /** 전일 종가 (KRW) */
    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice;
    
    /** 
     * 전일 대비 변화 방향
     * RISE: 상승, FALL: 하락, EVEN: 보합
     */
    @JsonProperty("change")
    private String change;
    
    /** 전일 대비 변화량 절대값 (KRW) */
    @JsonProperty("change_price")
    private Double changePrice;
    
    /** 전일 대비 변화율 절대값 (0.01 = 1%) */
    @JsonProperty("change_rate")
    private Double changeRate;
    
    /** 전일 대비 변화량 (상승: +, 하락: -) */
    @JsonProperty("signed_change_price")
    private Double signedChangePrice;
    
    /** 전일 대비 변화율 (상승: +, 하락: -) */
    @JsonProperty("signed_change_rate")
    private Double signedChangeRate;
    
    /** 가장 최근 거래량 (BTC) */
    @JsonProperty("trade_volume")
    private Double tradeVolume;
    
    /** 누적 거래대금 (당일 자정 기준, KRW) */
    @JsonProperty("acc_trade_price")
    private Double accTradePrice;
    
    /** 24시간 누적 거래대금 (KRW) */
    @JsonProperty("acc_trade_price_24h")
    private Double accTradePrice24h;
    
    /** 누적 거래량 (당일 자정 기준, BTC) */
    @JsonProperty("acc_trade_volume")
    private Double accTradeVolume;
    
    /** 24시간 누적 거래량 (BTC) */
    @JsonProperty("acc_trade_volume_24h")
    private Double accTradeVolume24h;
    
    /** 52주 신고가 (KRW) */
    @JsonProperty("highest_52_week_price")
    private Double highest52WeekPrice;
    
    /** 52주 신고가 달성일 (YYYY-MM-DD 형식) */
    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate;
    
    /** 52주 신저가 (KRW) */
    @JsonProperty("lowest_52_week_price")
    private Double lowest52WeekPrice;
    
    /** 52주 신저가 달성일 (YYYY-MM-DD 형식) */
    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate;
    
    /** 타임스탬프 */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 기본 생성자
     * Jackson 라이브러리에서 JSON 역직렬화할 때 필요
     */
    public BitcoinTickerDTO() {}

    // ========== Getter와 Setter 메서드들 ==========
    // 각 필드에 대한 접근자(accessor) 메서드들
    
    /**
     * 마켓 코드를 반환합니다.
     * @return 마켓 코드 (예: "KRW-BTC")
     */
    public String getMarket() { return market; }
    
    /**
     * 마켓 코드를 설정합니다.
     * @param market 마켓 코드
     */
    public void setMarket(String market) { this.market = market; }

    /**
     * 거래 일자를 반환합니다.
     * @return 거래 일자 (YYYYMMDD 형식)
     */
    public String getTradeDate() { return tradeDate; }
    public void setTradeDate(String tradeDate) { this.tradeDate = tradeDate; }

    /**
     * 거래 시각을 반환합니다.
     * @return 거래 시각 (HHMMSS 형식)
     */
    public String getTradeTime() { return tradeTime; }
    public void setTradeTime(String tradeTime) { this.tradeTime = tradeTime; }

    public String getTradeDateKst() { return tradeDateKst; }
    public void setTradeDateKst(String tradeDateKst) { this.tradeDateKst = tradeDateKst; }

    public String getTradeTimeKst() { return tradeTimeKst; }
    public void setTradeTimeKst(String tradeTimeKst) { this.tradeTimeKst = tradeTimeKst; }

    /**
     * 거래 타임스탬프를 반환합니다.
     * @return Unix 타임스탬프 (밀리초)
     */
    public Long getTradeTimestamp() { return tradeTimestamp; }
    public void setTradeTimestamp(Long tradeTimestamp) { this.tradeTimestamp = tradeTimestamp; }

    /**
     * 시가를 반환합니다.
     * @return 시가 (KRW)
     */
    public Double getOpeningPrice() { return openingPrice; }
    public void setOpeningPrice(Double openingPrice) { this.openingPrice = openingPrice; }

    /**
     * 고가를 반환합니다.
     * @return 고가 (KRW)
     */
    public Double getHighPrice() { return highPrice; }
    public void setHighPrice(Double highPrice) { this.highPrice = highPrice; }

    /**
     * 저가를 반환합니다.
     * @return 저가 (KRW)
     */
    public Double getLowPrice() { return lowPrice; }
    public void setLowPrice(Double lowPrice) { this.lowPrice = lowPrice; }

    /**
     * 현재가(최근 거래가)를 반환합니다.
     * @return 현재가 (KRW)
     */
    public Double getTradePrice() { return tradePrice; }
    public void setTradePrice(Double tradePrice) { this.tradePrice = tradePrice; }

    /**
     * 전일 종가를 반환합니다.
     * @return 전일 종가 (KRW)
     */
    public Double getPrevClosingPrice() { return prevClosingPrice; }
    public void setPrevClosingPrice(Double prevClosingPrice) { this.prevClosingPrice = prevClosingPrice; }

    /**
     * 전일 대비 변화 방향을 반환합니다.
     * @return "RISE"(상승), "FALL"(하락), "EVEN"(보합)
     */
    public String getChange() { return change; }
    public void setChange(String change) { this.change = change; }

    /**
     * 전일 대비 변화량 절대값을 반환합니다.
     * @return 변화량 절대값 (KRW)
     */
    public Double getChangePrice() { return changePrice; }
    public void setChangePrice(Double changePrice) { this.changePrice = changePrice; }

    /**
     * 전일 대비 변화율 절대값을 반환합니다.
     * @return 변화율 (0.01 = 1%)
     */
    public Double getChangeRate() { return changeRate; }
    public void setChangeRate(Double changeRate) { this.changeRate = changeRate; }

    /**
     * 전일 대비 변화량(부호 포함)을 반환합니다.
     * @return 변화량 (상승: +, 하락: -)
     */
    public Double getSignedChangePrice() { return signedChangePrice; }
    public void setSignedChangePrice(Double signedChangePrice) { this.signedChangePrice = signedChangePrice; }

    /**
     * 전일 대비 변화율(부호 포함)을 반환합니다.
     * @return 변화율 (상승: +, 하락: -)
     */
    public Double getSignedChangeRate() { return signedChangeRate; }
    public void setSignedChangeRate(Double signedChangeRate) { this.signedChangeRate = signedChangeRate; }

    /**
     * 최근 거래량을 반환합니다.
     * @return 거래량 (BTC)
     */
    public Double getTradeVolume() { return tradeVolume; }
    public void setTradeVolume(Double tradeVolume) { this.tradeVolume = tradeVolume; }

    /**
     * 누적 거래대금을 반환합니다.
     * @return 누적 거래대금 (KRW)
     */
    public Double getAccTradePrice() { return accTradePrice; }
    public void setAccTradePrice(Double accTradePrice) { this.accTradePrice = accTradePrice; }

    /**
     * 24시간 누적 거래대금을 반환합니다.
     * @return 24시간 누적 거래대금 (KRW)
     */
    public Double getAccTradePrice24h() { return accTradePrice24h; }
    public void setAccTradePrice24h(Double accTradePrice24h) { this.accTradePrice24h = accTradePrice24h; }

    public Double getAccTradeVolume() { return accTradeVolume; }
    public void setAccTradeVolume(Double accTradeVolume) { this.accTradeVolume = accTradeVolume; }

    public Double getAccTradeVolume24h() { return accTradeVolume24h; }
    public void setAccTradeVolume24h(Double accTradeVolume24h) { this.accTradeVolume24h = accTradeVolume24h; }

    /**
     * 52주 최고가를 반환합니다.
     * @return 52주 최고가 (KRW)
     */
    public Double getHighest52WeekPrice() { return highest52WeekPrice; }
    public void setHighest52WeekPrice(Double highest52WeekPrice) { this.highest52WeekPrice = highest52WeekPrice; }

    /**
     * 52주 최고가 달성일을 반환합니다.
     * @return 최고가 달성일 (YYYY-MM-DD 형식)
     */
    public String getHighest52WeekDate() { return highest52WeekDate; }
    public void setHighest52WeekDate(String highest52WeekDate) { this.highest52WeekDate = highest52WeekDate; }

    /**
     * 52주 최저가를 반환합니다.
     * @return 52주 최저가 (KRW)
     */
    public Double getLowest52WeekPrice() { return lowest52WeekPrice; }
    public void setLowest52WeekPrice(Double lowest52WeekPrice) { this.lowest52WeekPrice = lowest52WeekPrice; }

    /**
     * 52주 최저가 달성일을 반환합니다.
     * @return 최저가 달성일 (YYYY-MM-DD 형식)
     */
    public String getLowest52WeekDate() { return lowest52WeekDate; }
    public void setLowest52WeekDate(String lowest52WeekDate) { this.lowest52WeekDate = lowest52WeekDate; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    /**
     * 객체의 문자열 표현을 반환합니다.
     * 디버깅과 로깅에 유용합니다.
     * 
     * @return 객체의 모든 필드 값을 포함한 문자열
     */
    @Override
    public String toString() {
        return "BitcoinTickerDTO{" +
                "market='" + market + '\'' +
                ", tradeDate='" + tradeDate + '\'' +
                ", tradeTime='" + tradeTime + '\'' +
                ", tradeDateKst='" + tradeDateKst + '\'' +
                ", tradeTimeKst='" + tradeTimeKst + '\'' +
                ", tradeTimestamp=" + tradeTimestamp +
                ", openingPrice=" + openingPrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", tradePrice=" + tradePrice +
                ", prevClosingPrice=" + prevClosingPrice +
                ", change='" + change + '\'' +
                ", changePrice=" + changePrice +
                ", changeRate=" + changeRate +
                ", signedChangePrice=" + signedChangePrice +
                ", signedChangeRate=" + signedChangeRate +
                ", tradeVolume=" + tradeVolume +
                ", accTradePrice=" + accTradePrice +
                ", accTradePrice24h=" + accTradePrice24h +
                ", accTradeVolume=" + accTradeVolume +
                ", accTradeVolume24h=" + accTradeVolume24h +
                ", highest52WeekPrice=" + highest52WeekPrice +
                ", highest52WeekDate='" + highest52WeekDate + '\'' +
                ", lowest52WeekPrice=" + lowest52WeekPrice +
                ", lowest52WeekDate='" + lowest52WeekDate + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

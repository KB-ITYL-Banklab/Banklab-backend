package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 업비트 캔들(봉) 데이터 DTO
 */
@Data
public class UpbitCandleDto {
    
    /**
     * 마켓명
     */
    private String market;
    
    /**
     * 캔들 기준 시각 (UTC 기준)
     */
    @JsonProperty("candle_date_time_utc")
    private String candleDateTimeUtc;
    
    /**
     * 캔들 기준 시각 (KST 기준)
     */
    @JsonProperty("candle_date_time_kst")
    private String candleDateTimeKst;
    
    /**
     * 시가
     */
    @JsonProperty("opening_price")
    private Double openingPrice;
    
    /**
     * 고가
     */
    @JsonProperty("high_price")
    private Double highPrice;
    
    /**
     * 저가
     */
    @JsonProperty("low_price")
    private Double lowPrice;
    
    /**
     * 종가
     */
    @JsonProperty("trade_price")
    private Double tradePrice;
    
    /**
     * 해당 캔들에서 마지막 틱이 저장된 시각
     */
    private Long timestamp;
    
    /**
     * 누적 거래 금액
     */
    @JsonProperty("candle_acc_trade_price")
    private Double candleAccTradePrice;
    
    /**
     * 누적 거래량
     */
    @JsonProperty("candle_acc_trade_volume")
    private Double candleAccTradeVolume;
    
    /**
     * 전일 종가 (일봉에서만 제공)
     */
    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice;
    
    /**
     * 전일 종가 대비 변화량 (일봉에서만 제공)
     */
    @JsonProperty("change_price")
    private Double changePrice;
    
    /**
     * 전일 종가 대비 변화율 (일봉에서만 제공)
     */
    @JsonProperty("change_rate")
    private Double changeRate;
    
    /**
     * 종가 전일 대비 (일봉에서만 제공)
     * RISE : 상승
     * EVEN : 보합  
     * FALL : 하락
     */
    @JsonProperty("converted_trade_price")
    private Double convertedTradePrice;
}

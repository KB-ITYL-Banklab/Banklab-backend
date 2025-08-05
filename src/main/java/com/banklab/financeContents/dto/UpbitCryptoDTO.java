package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 업비트 API 가상화폐 시세 정보를 담는 DTO 클래스
 * finance_upbit 테이블과 매핑되는 데이터 전송 객체
 */
public class UpbitCryptoDTO {
    
    /** 고유 ID */
    private Long id;
    
    /** 마켓 코드 (예: KRW-BTC) */
    @JsonProperty("market")
    private String market;
    
    /** 시가 */
    @JsonProperty("opening_price")
    private Double openingPrice;
    
    /** 종가 (현재가) */
    @JsonProperty("trade_price")
    private Double tradePrice;
    
    /** 전일 종가 */
    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice;
    
    /** 전일 대비 등락률 (비율) */
    @JsonProperty("signed_change_rate")
    private Double changeRate;
    
    /** 24시간 누적 거래량 */
    @JsonProperty("acc_trade_volume_24h")
    private Double accTradeVolume24h;
    
    /** 24시간 누적 거래대금 */
    @JsonProperty("acc_trade_price_24h")
    private Double accTradePrice24h;
    
    /** 데이터 생성 시각 */
    private LocalDateTime createdAt;
    
    /** 데이터 수정 시각 */
    private LocalDateTime updatedAt;

    // 기본 생성자
    public UpbitCryptoDTO() {}

    // 전체 생성자
    public UpbitCryptoDTO(String market, Double openingPrice, Double tradePrice, 
                         Double prevClosingPrice, Double changeRate, 
                         Double accTradeVolume24h, Double accTradePrice24h) {
        this.market = market;
        this.openingPrice = openingPrice;
        this.tradePrice = tradePrice;
        this.prevClosingPrice = prevClosingPrice;
        this.changeRate = changeRate;
        this.accTradeVolume24h = accTradeVolume24h;
        this.accTradePrice24h = accTradePrice24h;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(Double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public Double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(Double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public Double getPrevClosingPrice() {
        return prevClosingPrice;
    }

    public void setPrevClosingPrice(Double prevClosingPrice) {
        this.prevClosingPrice = prevClosingPrice;
    }

    public Double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(Double changeRate) {
        this.changeRate = changeRate;
    }

    public Double getAccTradeVolume24h() {
        return accTradeVolume24h;
    }

    public void setAccTradeVolume24h(Double accTradeVolume24h) {
        this.accTradeVolume24h = accTradeVolume24h;
    }

    public Double getAccTradePrice24h() {
        return accTradePrice24h;
    }

    public void setAccTradePrice24h(Double accTradePrice24h) {
        this.accTradePrice24h = accTradePrice24h;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UpbitCryptoDTO{" +
                "id=" + id +
                ", market='" + market + '\'' +
                ", openingPrice=" + openingPrice +
                ", tradePrice=" + tradePrice +
                ", prevClosingPrice=" + prevClosingPrice +
                ", changeRate=" + changeRate +
                ", accTradeVolume24h=" + accTradeVolume24h +
                ", accTradePrice24h=" + accTradePrice24h +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

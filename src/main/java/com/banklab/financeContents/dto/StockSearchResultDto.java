package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주식 검색 결과 DTO
 * JSON 직렬화에 안전한 형태로 데이터를 전달하기 위한 클래스
 * 새로운 테이블 구조에 맞게 업데이트됨
 */
@JsonInclude(JsonInclude.Include.ALWAYS) // null 값도 포함
public class StockSearchResultDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("baseDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate baseDate;
    
    @JsonProperty("stockCode")
    private String stockCode;
    
    @JsonProperty("isinCode")
    private String isinCode;
    
    @JsonProperty("stockName")
    private String stockName;
    
    @JsonProperty("marketCategory")
    private String marketCategory;
    
    @JsonProperty("closingPrice")
    private Long closingPrice;
    
    @JsonProperty("versus")
    private Long versus;
    
    @JsonProperty("fluctuationRate")
    private BigDecimal fluctuationRate;
    
    @JsonProperty("marketPrice")
    private Long marketPrice;
    
    @JsonProperty("highPrice")
    private Long highPrice;
    
    @JsonProperty("lowPrice")
    private Long lowPrice;
    
    @JsonProperty("tradingQuantity")
    private Long tradingQuantity;
    
    @JsonProperty("tradingPrice")
    private Long tradingPrice;
    
    @JsonProperty("listedStockCount")
    private Long listedStockCount;
    
    @JsonProperty("marketTotalAmount")
    private Long marketTotalAmount;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 기본 생성자
    public StockSearchResultDto() {}
    
    // FinanceStockVO를 변환하는 생성자
    public StockSearchResultDto(com.banklab.financeContents.domain.FinanceStockVO vo) {
        if (vo != null) {
            this.id = vo.getId();
            this.baseDate = vo.getBasDt();
            this.stockCode = cleanString(vo.getSrtnCd());
            this.isinCode = cleanString(vo.getIsinCd());
            this.stockName = cleanString(vo.getItmsNm());
            this.marketCategory = cleanString(vo.getMrktCtg());
            this.closingPrice = vo.getClpr();
            this.versus = vo.getVs();
            this.fluctuationRate = vo.getFltRt();
            this.marketPrice = vo.getMkp();
            this.highPrice = vo.getHipr();
            this.lowPrice = vo.getLopr();
            this.tradingQuantity = vo.getTrqu();
            this.tradingPrice = vo.getTrPrc();
            this.listedStockCount = vo.getLstgStCnt();
            this.marketTotalAmount = vo.getMrktTotAmt();
            this.createdAt = vo.getCreatedAt();
        }
    }
    
    /**
     * 문자열을 안전하게 정리하는 메서드
     */
    private String cleanString(String str) {
        if (str == null) {
            return null;
        }
        // 특수 문자나 제어 문자 제거
        return str.trim()
                  .replaceAll("[\\x00-\\x1F\\x7F]", "") // 제어 문자 제거
                  .replaceAll("\\p{C}", ""); // 모든 제어 문자 제거
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getBaseDate() { return baseDate; }
    public void setBaseDate(LocalDate baseDate) { this.baseDate = baseDate; }
    
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    
    public String getIsinCode() { return isinCode; }
    public void setIsinCode(String isinCode) { this.isinCode = isinCode; }
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public String getMarketCategory() { return marketCategory; }
    public void setMarketCategory(String marketCategory) { this.marketCategory = marketCategory; }
    
    public Long getClosingPrice() { return closingPrice; }
    public void setClosingPrice(Long closingPrice) { this.closingPrice = closingPrice; }
    
    public Long getVersus() { return versus; }
    public void setVersus(Long versus) { this.versus = versus; }
    
    public BigDecimal getFluctuationRate() { return fluctuationRate; }
    public void setFluctuationRate(BigDecimal fluctuationRate) { this.fluctuationRate = fluctuationRate; }
    
    public Long getMarketPrice() { return marketPrice; }
    public void setMarketPrice(Long marketPrice) { this.marketPrice = marketPrice; }
    
    public Long getHighPrice() { return highPrice; }
    public void setHighPrice(Long highPrice) { this.highPrice = highPrice; }
    
    public Long getLowPrice() { return lowPrice; }
    public void setLowPrice(Long lowPrice) { this.lowPrice = lowPrice; }
    
    public Long getTradingQuantity() { return tradingQuantity; }
    public void setTradingQuantity(Long tradingQuantity) { this.tradingQuantity = tradingQuantity; }
    
    public Long getTradingPrice() { return tradingPrice; }
    public void setTradingPrice(Long tradingPrice) { this.tradingPrice = tradingPrice; }
    
    public Long getListedStockCount() { return listedStockCount; }
    public void setListedStockCount(Long listedStockCount) { this.listedStockCount = listedStockCount; }
    
    public Long getMarketTotalAmount() { return marketTotalAmount; }
    public void setMarketTotalAmount(Long marketTotalAmount) { this.marketTotalAmount = marketTotalAmount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

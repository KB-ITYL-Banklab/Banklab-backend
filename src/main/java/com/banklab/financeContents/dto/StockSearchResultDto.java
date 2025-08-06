package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * 주식 검색 결과 DTO
 * JSON 직렬화에 안전한 형태로 데이터를 전달하기 위한 클래스
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
    
    @JsonProperty("stockName")
    private String stockName;
    
    @JsonProperty("beginFluctuationRate")
    private Double beginFluctuationRate;
    
    @JsonProperty("endFluctuationRate")
    private Double endFluctuationRate;
    
    @JsonProperty("beginVersus")
    private Double beginVersus;
    
    @JsonProperty("endVersus")
    private Double endVersus;
    
    @JsonProperty("beginTradingQuantity")
    private Long beginTradingQuantity;
    
    @JsonProperty("endTradingQuantity")
    private Long endTradingQuantity;
    
    @JsonProperty("beginTradingPrice")
    private Long beginTradingPrice;
    
    @JsonProperty("endTradingPrice")
    private Long endTradingPrice;
    
    // 기본 생성자
    public StockSearchResultDto() {}
    
    // FinanceStockVO를 변환하는 생성자
    public StockSearchResultDto(com.banklab.financeContents.domain.FinanceStockVO vo) {
        if (vo != null) {
            this.id = vo.getId();
            this.baseDate = vo.getBasDt();
            this.stockCode = cleanString(vo.getSrtnCd());
            this.stockName = cleanString(vo.getItmsNm());
            this.beginFluctuationRate = vo.getBeginFltRt();
            this.endFluctuationRate = vo.getEndFltRt();
            this.beginVersus = vo.getBeginVs();
            this.endVersus = vo.getEndVs();
            this.beginTradingQuantity = vo.getBeginTrqu();
            this.endTradingQuantity = vo.getEndTrqu();
            this.beginTradingPrice = vo.getBeginTrPrc();
            this.endTradingPrice = vo.getEndTrPrc();
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
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public Double getBeginFluctuationRate() { return beginFluctuationRate; }
    public void setBeginFluctuationRate(Double beginFluctuationRate) { this.beginFluctuationRate = beginFluctuationRate; }
    
    public Double getEndFluctuationRate() { return endFluctuationRate; }
    public void setEndFluctuationRate(Double endFluctuationRate) { this.endFluctuationRate = endFluctuationRate; }
    
    public Double getBeginVersus() { return beginVersus; }
    public void setBeginVersus(Double beginVersus) { this.beginVersus = beginVersus; }
    
    public Double getEndVersus() { return endVersus; }
    public void setEndVersus(Double endVersus) { this.endVersus = endVersus; }
    
    public Long getBeginTradingQuantity() { return beginTradingQuantity; }
    public void setBeginTradingQuantity(Long beginTradingQuantity) { this.beginTradingQuantity = beginTradingQuantity; }
    
    public Long getEndTradingQuantity() { return endTradingQuantity; }
    public void setEndTradingQuantity(Long endTradingQuantity) { this.endTradingQuantity = endTradingQuantity; }
    
    public Long getBeginTradingPrice() { return beginTradingPrice; }
    public void setBeginTradingPrice(Long beginTradingPrice) { this.beginTradingPrice = beginTradingPrice; }
    
    public Long getEndTradingPrice() { return endTradingPrice; }
    public void setEndTradingPrice(Long endTradingPrice) { this.endTradingPrice = endTradingPrice; }
}

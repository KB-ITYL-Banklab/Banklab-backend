package com.banklab.financeContents.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주식 정보 도메인 객체
 * finance_stock 테이블과 매핑되는 VO 클래스
 */
public class FinanceStockVO {
    
    /** 고유 ID */
    @JsonProperty("id")
    private Long id;
    
    /** 기준일자 */
    @JsonProperty("baseDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate basDt;
    
    /** 종목코드 */
    @JsonProperty("stockCode")
    private String srtnCd;
    
    /** 종목이름(한글) */
    @JsonProperty("stockName")
    private String itmsNm;
    
    /** 시작 등락률 */
    @JsonProperty("beginFluctuationRate")
    private Double beginFltRt;
    
    /** 종료 등락률 */
    @JsonProperty("endFluctuationRate")
    private Double endFltRt;
    
    /** 시작 대비 등락폭 */
    @JsonProperty("beginVersus")
    private Double beginVs;
    
    /** 종료 대비 등락폭 */
    @JsonProperty("endVersus")
    private Double endVs;
    
    /** 시작 거래량 */
    @JsonProperty("beginTradingQuantity")
    private Long beginTrqu;
    
    /** 종료 거래량 */
    @JsonProperty("endTradingQuantity")
    private Long endTrqu;
    
    /** 시작 거래대금 */
    @JsonProperty("beginTradingPrice")
    private Long beginTrPrc;
    
    /** 종료 거래대금 */
    @JsonProperty("endTradingPrice")
    private Long endTrPrc;
    
    /** 생성시간 */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 수정시간 */
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public FinanceStockVO() {}
    
    // 전체 필드 생성자
    public FinanceStockVO(Long id, LocalDate basDt, String srtnCd, String itmsNm, 
                         Double beginFltRt, Double endFltRt, Double beginVs, Double endVs,
                         Long beginTrqu, Long endTrqu, Long beginTrPrc, Long endTrPrc,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.basDt = basDt;
        this.srtnCd = srtnCd;
        this.itmsNm = itmsNm;
        this.beginFltRt = beginFltRt;
        this.endFltRt = endFltRt;
        this.beginVs = beginVs;
        this.endVs = endVs;
        this.beginTrqu = beginTrqu;
        this.endTrqu = endTrqu;
        this.beginTrPrc = beginTrPrc;
        this.endTrPrc = endTrPrc;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter & Setter 메서드들
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getBasDt() {
        return basDt;
    }
    
    public void setBasDt(LocalDate basDt) {
        this.basDt = basDt;
    }
    
    public String getSrtnCd() {
        return srtnCd;
    }
    
    public void setSrtnCd(String srtnCd) {
        this.srtnCd = srtnCd;
    }
    
    public String getItmsNm() {
        return itmsNm;
    }
    
    public void setItmsNm(String itmsNm) {
        this.itmsNm = itmsNm;
    }
    
    public Double getBeginFltRt() {
        return beginFltRt;
    }
    
    public void setBeginFltRt(Double beginFltRt) {
        this.beginFltRt = beginFltRt;
    }
    
    public Double getEndFltRt() {
        return endFltRt;
    }
    
    public void setEndFltRt(Double endFltRt) {
        this.endFltRt = endFltRt;
    }
    
    public Double getBeginVs() {
        return beginVs;
    }
    
    public void setBeginVs(Double beginVs) {
        this.beginVs = beginVs;
    }
    
    public Double getEndVs() {
        return endVs;
    }
    
    public void setEndVs(Double endVs) {
        this.endVs = endVs;
    }
    
    public Long getBeginTrqu() {
        return beginTrqu;
    }
    
    public void setBeginTrqu(Long beginTrqu) {
        this.beginTrqu = beginTrqu;
    }
    
    public Long getEndTrqu() {
        return endTrqu;
    }
    
    public void setEndTrqu(Long endTrqu) {
        this.endTrqu = endTrqu;
    }
    
    public Long getBeginTrPrc() {
        return beginTrPrc;
    }
    
    public void setBeginTrPrc(Long beginTrPrc) {
        this.beginTrPrc = beginTrPrc;
    }
    
    public Long getEndTrPrc() {
        return endTrPrc;
    }
    
    public void setEndTrPrc(Long endTrPrc) {
        this.endTrPrc = endTrPrc;
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
        return "FinanceStockVO{" +
                "id=" + id +
                ", basDt=" + basDt +
                ", srtnCd='" + srtnCd + '\'' +
                ", itmsNm='" + itmsNm + '\'' +
                ", beginFltRt=" + beginFltRt +
                ", endFltRt=" + endFltRt +
                ", beginVs=" + beginVs +
                ", endVs=" + endVs +
                ", beginTrqu=" + beginTrqu +
                ", endTrqu=" + endTrqu +
                ", beginTrPrc=" + beginTrPrc +
                ", endTrPrc=" + endTrPrc +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

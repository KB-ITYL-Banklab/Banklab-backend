package com.banklab.financeContents.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
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
    @JsonProperty("basDt")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate basDt;
    
    /** 단축코드 (종목코드) */
    @JsonProperty("srtnCd")
    private String srtnCd;
    
    /** 국제증권식별번호 (ISIN) */
    @JsonProperty("isinCd")
    private String isinCd;
    
    /** 종목명 */
    @JsonProperty("itmsNm")
    private String itmsNm;
    
    /** 소속 시장 (KOSDAQ 등) */
    @JsonProperty("mrktCtg")
    private String mrktCtg;
    
    /** 종가 (원) */
    @JsonProperty("clpr")
    private Long clpr;
    
    /** 전일 대비 등락 */
    @JsonProperty("vs")
    private Long vs;
    
    /** 전일 대비 등락률 (%) */
    @JsonProperty("fltRt")
    private BigDecimal fltRt;
    
    /** 시가 (원) */
    @JsonProperty("mkp")
    private Long mkp;
    
    /** 고가 (원) */
    @JsonProperty("hipr")
    private Long hipr;
    
    /** 저가 (원) */
    @JsonProperty("lopr")
    private Long lopr;
    
    /** 거래량 (주) */
    @JsonProperty("trqu")
    private Long trqu;
    
    /** 거래대금 (원) */
    @JsonProperty("trPrc")
    private Long trPrc;
    
    /** 상장 주식 수 */
    @JsonProperty("lstgStCnt")
    private Long lstgStCnt;
    
    /** 시가총액 (원) */
    @JsonProperty("mrktTotAmt")
    private Long mrktTotAmt;
    
    /** 데이터 생성 시각 */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 기본 생성자
    public FinanceStockVO() {}
    
    // 전체 필드 생성자
    public FinanceStockVO(Long id, LocalDate basDt, String srtnCd, String isinCd, String itmsNm, 
                         String mrktCtg, Long clpr, Long vs, BigDecimal fltRt, Long mkp, Long hipr, 
                         Long lopr, Long trqu, Long trPrc, Long lstgStCnt, Long mrktTotAmt, 
                         LocalDateTime createdAt) {
        this.id = id;
        this.basDt = basDt;
        this.srtnCd = srtnCd;
        this.isinCd = isinCd;
        this.itmsNm = itmsNm;
        this.mrktCtg = mrktCtg;
        this.clpr = clpr;
        this.vs = vs;
        this.fltRt = fltRt;
        this.mkp = mkp;
        this.hipr = hipr;
        this.lopr = lopr;
        this.trqu = trqu;
        this.trPrc = trPrc;
        this.lstgStCnt = lstgStCnt;
        this.mrktTotAmt = mrktTotAmt;
        this.createdAt = createdAt;
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
    
    public String getIsinCd() {
        return isinCd;
    }
    
    public void setIsinCd(String isinCd) {
        this.isinCd = isinCd;
    }
    
    public String getItmsNm() {
        return itmsNm;
    }
    
    public void setItmsNm(String itmsNm) {
        this.itmsNm = itmsNm;
    }
    
    public String getMrktCtg() {
        return mrktCtg;
    }
    
    public void setMrktCtg(String mrktCtg) {
        this.mrktCtg = mrktCtg;
    }
    
    public Long getClpr() {
        return clpr;
    }
    
    public void setClpr(Long clpr) {
        this.clpr = clpr;
    }
    
    public Long getVs() {
        return vs;
    }
    
    public void setVs(Long vs) {
        this.vs = vs;
    }
    
    public BigDecimal getFltRt() {
        return fltRt;
    }
    
    public void setFltRt(BigDecimal fltRt) {
        this.fltRt = fltRt;
    }
    
    public Long getMkp() {
        return mkp;
    }
    
    public void setMkp(Long mkp) {
        this.mkp = mkp;
    }
    
    public Long getHipr() {
        return hipr;
    }
    
    public void setHipr(Long hipr) {
        this.hipr = hipr;
    }
    
    public Long getLopr() {
        return lopr;
    }
    
    public void setLopr(Long lopr) {
        this.lopr = lopr;
    }
    
    public Long getTrqu() {
        return trqu;
    }
    
    public void setTrqu(Long trqu) {
        this.trqu = trqu;
    }
    
    public Long getTrPrc() {
        return trPrc;
    }
    
    public void setTrPrc(Long trPrc) {
        this.trPrc = trPrc;
    }
    
    public Long getLstgStCnt() {
        return lstgStCnt;
    }
    
    public void setLstgStCnt(Long lstgStCnt) {
        this.lstgStCnt = lstgStCnt;
    }
    
    public Long getMrktTotAmt() {
        return mrktTotAmt;
    }
    
    public void setMrktTotAmt(Long mrktTotAmt) {
        this.mrktTotAmt = mrktTotAmt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "FinanceStockVO{" +
                "id=" + id +
                ", basDt=" + basDt +
                ", srtnCd='" + srtnCd + '\'' +
                ", isinCd='" + isinCd + '\'' +
                ", itmsNm='" + itmsNm + '\'' +
                ", mrktCtg='" + mrktCtg + '\'' +
                ", clpr=" + clpr +
                ", vs=" + vs +
                ", fltRt=" + fltRt +
                ", mkp=" + mkp +
                ", hipr=" + hipr +
                ", lopr=" + lopr +
                ", trqu=" + trqu +
                ", trPrc=" + trPrc +
                ", lstgStCnt=" + lstgStCnt +
                ", mrktTotAmt=" + mrktTotAmt +
                ", createdAt=" + createdAt +
                '}';
    }
}

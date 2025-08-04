package com.banklab.calculator.domain;

/**
 * 세금 유형
 */
public enum TaxType {
    GENERAL("일반과세", 0.154),     // 15.4%
    PREFERENTIAL("우대과세", 0.095), // 9.5%
    EXEMPT("비과세", 0.0);          // 0%
    
    private final String description;
    private final double rate;
    
    TaxType(String description, double rate) {
        this.description = description;
        this.rate = rate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getRate() {
        return rate;
    }
}

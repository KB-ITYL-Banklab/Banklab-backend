package com.banklab.calculator.domain;

/**
 * 대출 상환 방식
 */
public enum LoanType {
    EQUAL_PAYMENT("원리금균등상환"),
    EQUAL_PRINCIPAL("원금균등상환"),
    BULLET_PAYMENT("원금만기일시상환");
    
    private final String description;
    
    LoanType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

package com.banklab.calculator.domain;

/**
 * 적금 방식
 */
public enum SavingsType {
    MATURITY_AMOUNT("만기금액 계산"),   // 정액 적립식
    TARGET_AMOUNT("목표금액 계산");    // 목표 달성형

    private final String description;

    SavingsType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


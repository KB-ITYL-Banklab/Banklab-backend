package com.banklab.typetest.domain.enums;

// 제약조건 타입 enum
public enum ConstraintType {
    PRINCIPAL_GUARANTEE,     // 원금보장 필수
    HIGH_RISK_FORBIDDEN,     // 고위험 상품 금지
    LIQUIDITY_REQUIRED       // 유동성 필수
}

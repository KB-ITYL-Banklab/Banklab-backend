package com.banklab.product.service;

import com.banklab.product.dto.savings.SavingsWithOptionsDto;

public interface SavingsOptionsService {
    
    /**
     * 특정 적금 상품의 모든 옵션 조회
     */
    SavingsWithOptionsDto getSavingsWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

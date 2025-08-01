package com.banklab.product.service;

import com.banklab.product.dto.savings.SavingsWithOptionsDto;

public interface SavingsDetailService {
    
    /**
     * 특정 적금 상품의 모든 정보 조회
     */
    SavingsWithOptionsDto getSavingsWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

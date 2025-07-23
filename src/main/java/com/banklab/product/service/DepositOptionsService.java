package com.banklab.product.service;

import com.banklab.product.dto.deposit.DepositWithOptionsDto;

public interface DepositOptionsService {
    
    /**
     * 특정 예금 상품의 모든 옵션 조회
     */
    DepositWithOptionsDto getDepositWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

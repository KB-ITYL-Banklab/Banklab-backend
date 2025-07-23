package com.banklab.product.service;

import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;

public interface CreditLoanOptionsService {
    
    /**
     * 특정 신용대출 상품의 모든 옵션 조회
     */
    CreditLoanWithOptionsDto getCreditLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

package com.banklab.product.service.creditloan;

import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;

public interface CreditLoanDetailService {
    
    /**
     * 특정 신용대출 상품의 모든 정보 조회
     */
    CreditLoanWithOptionsDto getCreditLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

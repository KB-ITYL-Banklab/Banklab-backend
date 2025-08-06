package com.banklab.product.service.mortgage;

import com.banklab.product.dto.mortgage.MortgageLoanWithOptionsDto;


public interface MortgageLoanDetailService {

    MortgageLoanWithOptionsDto getMortgageLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);

}

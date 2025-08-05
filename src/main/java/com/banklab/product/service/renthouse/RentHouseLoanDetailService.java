package com.banklab.product.service.renthouse;

import com.banklab.product.dto.renthouse.RentHouseLoanWithOptionsDto;

public interface RentHouseLoanDetailService {
    RentHouseLoanWithOptionsDto getRentHouseLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);

}



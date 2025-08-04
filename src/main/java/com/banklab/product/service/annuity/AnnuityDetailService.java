package com.banklab.product.service.annuity;

import com.banklab.product.dto.annuity.AnnuityWithOptionsDto;

public interface AnnuityDetailService {
    AnnuityWithOptionsDto getAnnuityProductsWithOptions(String dclsMonth, String finCoNo, String finPrdtCd);
}

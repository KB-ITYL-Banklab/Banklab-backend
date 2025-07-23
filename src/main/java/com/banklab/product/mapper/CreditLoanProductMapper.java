package com.banklab.product.mapper;

import com.banklab.product.domain.CreditLoanProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditLoanProductMapper {
    List<CreditLoanProduct> findAllCreditLoanProducts();
    void insertCreditLoanProduct(CreditLoanProduct product);
    void deleteAllCreditLoanProducts();
    /**
     * 특정 상품 조회
     */
    CreditLoanProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                       @Param("finCoNo") String finCoNo,
                                       @Param("finPrdtCd") String finPrdtCd);
}

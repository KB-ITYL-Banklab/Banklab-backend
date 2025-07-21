package com.banklab.product.mapper;

import com.banklab.product.domain.CreditLoanProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditLoanProductMapper {
    List<CreditLoanProduct> findAllCreditLoanProducts();
    void insertCreditLoanProduct(CreditLoanProduct product);
    void deleteAllCreditLoanProducts();
}

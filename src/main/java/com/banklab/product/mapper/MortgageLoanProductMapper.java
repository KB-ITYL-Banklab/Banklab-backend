package com.banklab.product.mapper;

import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.domain.mortgage.MortgageLoanProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MortgageLoanProductMapper {
    List<MortgageLoanProduct> findAllMortgageLaonProducts();

    void insertMortgageLoanProduct(MortgageLoanProduct mortgageLoanProduct);
    void deleteAllMortgageLoanProduct();

    MortgageLoanProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                         @Param("finCoNo") String finCoNo,
                                         @Param("finPrdtCd") String finPrdtCd);

    MortgageLoanProduct findById(@Param("id") Long id);

    void updateMortgageLoanProduct(MortgageLoanProduct mortgageLoanProduct);

}

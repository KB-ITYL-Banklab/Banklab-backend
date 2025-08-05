package com.banklab.product.mapper;

import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RentHouseLoanProductMapper {
    List<RentHouseLoanProduct> findAllRentHouseLoanProducts();

    void insertRentHouseLoanProduct(RentHouseLoanProduct rentHouseLoanProduct);
    void deleteAllRentHouseLoanProduct();

    RentHouseLoanProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                          @Param("finCoNo") String finCoNo,
                                          @Param("finPrdtCd") String finPrdtCd);

    RentHouseLoanProduct findById(@Param("id") Long id);

    /**
     * 상품 업데이트
     */
    void updateRentHouseLoanProduct(RentHouseLoanProduct rentHouseLoanProduct);
}

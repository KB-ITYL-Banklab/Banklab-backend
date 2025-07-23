package com.banklab.product.mapper;

import com.banklab.product.domain.SavingsProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SavingsProductMapper {
    List<SavingsProduct> findAllSavingsProducts();
    void insertSavingsProduct(SavingsProduct product);
    void deleteAllSavingsProducts();
    /**
     * 특정 상품 조회
     */
    SavingsProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                    @Param("finCoNo") String finCoNo,
                                    @Param("finPrdtCd") String finPrdtCd);

}

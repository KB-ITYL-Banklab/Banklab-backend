package com.banklab.product.mapper;

import com.banklab.product.domain.deposit.DepositProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepositProductMapper {
    List<DepositProduct> findAllDepositProducts();
    void insertDepositProduct(DepositProduct product);
    void deleteAllDepositProducts();
    /**
     * 특정 상품 조회
     */
    DepositProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                    @Param("finCoNo") String finCoNo,
                                    @Param("finPrdtCd") String finPrdtCd);
    
    /**
     * ID로 상품 조회
     */
    DepositProduct findById(@Param("id") Long id);
}

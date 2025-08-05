package com.banklab.product.mapper;

import com.banklab.product.domain.savings.SavingsProduct;
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
    
    /**
     * ID로 상품 조회
     */
    SavingsProduct findById(@Param("id") Long id);
    
    /**
     * 상품 업데이트
     */
    void updateSavingsProduct(SavingsProduct product);
}

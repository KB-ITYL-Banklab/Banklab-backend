package com.banklab.product.mapper;

import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.domain.savings.SavingsProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnuityProductMapper {
    
    List<AnnuityProduct> findAllAnnuityProducts();
    
    void insertAnnuityProduct(AnnuityProduct annuityProduct);

     void deleteAllAnnuityProducts();

    /**
     * 특정 상품 조회
     */
    AnnuityProduct findByProductKey(@Param("dclsMonth") String dclsMonth,
                                    @Param("finCoNo") String finCoNo,
                                    @Param("finPrdtCd") String finPrdtCd);

    /**
     * ID로 상품 조회
     */
    AnnuityProduct findById(@Param("id") Long id);
    

}

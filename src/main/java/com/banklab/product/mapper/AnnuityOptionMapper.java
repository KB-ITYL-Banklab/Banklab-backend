package com.banklab.product.mapper;

import com.banklab.product.domain.annuity.AnnuityOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnuityOptionMapper {
    
    List<AnnuityOption> findAllAnnuityOptions();

    void insertAnnuityOption(AnnuityOption annuityOption);
    void deleteAllAnnuityOptions();
    /**
     * 특정 상품의 모든 옵션 조회
     */
    List<AnnuityOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                             @Param("finCoNo") String finCoNo,
                                             @Param("finPrdtCd") String finPrdtCd);
}

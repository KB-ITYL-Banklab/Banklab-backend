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

    /**
     * 특정 옵션 조회
     */
    AnnuityOption findByOptionKey(@Param("dclsMonth") String dclsMonth,
                                  @Param("finCoNo") String finCoNo,
                                  @Param("finPrdtCd") String finPrdtCd,
                                  @Param("pnsnRecpTrm") String pnsnRecpTrm,
                                  @Param("pnsnEntrAge") String pnsnEntrAge,
                                  @Param("monPaymAtm") String monPaymAtm,
                                  @Param("paymPrd") String paymPrd,
                                  @Param("pnsnStrtAge") String pnsnStrtAge);

    /**
     * 옵션 업데이트
     */
    void updateAnnuityOption(AnnuityOption annuityOption);
}

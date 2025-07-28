package com.banklab.product.mapper;

import com.banklab.product.domain.SavingsOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SavingsOptionMapper {
    List<SavingsOption> findAllSavingsOptions();
    void insertSavingsOption(SavingsOption option);
    void deleteAllSavingsOptions();
    /**
     * 특정 상품의 모든 옵션 조회
     */
    List<SavingsOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                             @Param("finCoNo") String finCoNo,
                                             @Param("finPrdtCd") String finPrdtCd);
}

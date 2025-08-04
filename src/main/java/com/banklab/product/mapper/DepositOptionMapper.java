package com.banklab.product.mapper;

import com.banklab.product.domain.deposit.DepositOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DepositOptionMapper {
    List<DepositOption> findAllDepositOptions();
    void insertDepositOption(DepositOption option);
    void deleteAllDepositOptions();
    /**
     * 특정 상품의 모든 옵션 조회
     */
    List<DepositOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                             @Param("finCoNo") String finCoNo,
                                             @Param("finPrdtCd") String finPrdtCd);

}
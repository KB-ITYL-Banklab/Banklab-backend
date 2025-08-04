package com.banklab.product.mapper;

import com.banklab.product.domain.creditloan.CreditLoanOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditLoanOptionMapper {
    List<CreditLoanOption> findAllCreditLoanOptions();
    void insertCreditLoanOption(CreditLoanOption option);
    void deleteAllCreditLoanOptions();
    /**
     * 특정 상품의 모든 옵션 조회
     */
    List<CreditLoanOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                                @Param("finCoNo") String finCoNo,
                                                @Param("finPrdtCd") String finPrdtCd);


}

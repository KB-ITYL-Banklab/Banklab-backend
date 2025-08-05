package com.banklab.product.mapper;

import com.banklab.product.domain.mortgage.MortgageLoanOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MortgageLoanOptionMapper {

    List<MortgageLoanOption> findAllMortgageLoanOptions();

    void insertMortgageLoanOption(MortgageLoanOption option);

    void deleteAllMortgageLoanOptions();
    
    List<MortgageLoanOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                                  @Param("finCoNo") String finCoNo,
                                                  @Param("finPrdtCd") String finPrdtCd);
}

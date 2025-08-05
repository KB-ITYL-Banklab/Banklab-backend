package com.banklab.product.mapper;

import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RentHouseLoanOptionMapper {

    List<RentHouseLoanOption> findAllRentHouseLoanOptions();

    void insertRentHouseLoanOption(RentHouseLoanOption option);

    void deleteAllRentHouseLoanOptions();
    
    List<RentHouseLoanOption> findOptionsByProduct(@Param("dclsMonth") String dclsMonth,
                                                   @Param("finCoNo") String finCoNo,
                                                   @Param("finPrdtCd") String finPrdtCd);
}

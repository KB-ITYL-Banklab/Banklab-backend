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

    /**
     * 특정 옵션 조회
     */
    RentHouseLoanOption findByOptionKey(@Param("dclsMonth") String dclsMonth,
                                        @Param("finCoNo") String finCoNo,
                                        @Param("finPrdtCd") String finPrdtCd,
                                        @Param("rpayType") String rpayType,
                                        @Param("lendRateType") String lendRateType);

    /**
     * 옵션 업데이트
     */
    void updateRentHouseLoanOption(RentHouseLoanOption option);
}

package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.InvestmentType;
import org.apache.ibatis.annotations.Param;

public interface InvestmentTypeMapper {
    /**
     * 주어진 ID로 InvestmentType을 조회합니다.
     * @param id 조회할 InvestmentType의 ID
     * @return 해당 ID에 해당하는 InvestmentType 객체, 없으면 null
     */
    InvestmentType findById(@Param("id") Long id);
}
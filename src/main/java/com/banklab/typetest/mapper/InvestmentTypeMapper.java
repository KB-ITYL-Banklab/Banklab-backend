package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.InvestmentType;
import org.apache.ibatis.annotations.Param;

public interface InvestmentTypeMapper {
    InvestmentType findById(@Param("id") Long id);
}
package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentType;
import org.apache.ibatis.annotations.Param;

public interface UserInvestmentTypeMapper {
    UserInvestmentType findByUserId(@Param("userId") Long userId);
    void insertUserInvestmentType(UserInvestmentType userInvestmentType);
    void updateUserInvestmentType(UserInvestmentType userInvestmentType);
}

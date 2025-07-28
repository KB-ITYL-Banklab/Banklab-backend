package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserInvestmentProfileMapper {
    
    /**
     * 사용자 투자 프로필 조회
     */
    UserInvestmentProfile findByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 투자 프로필 저장
     */
    void insertUserInvestmentProfile(UserInvestmentProfile profile);
    
    /**
     * 사용자 투자 프로필 업데이트
     */
    void updateUserInvestmentProfile(UserInvestmentProfile profile);
}

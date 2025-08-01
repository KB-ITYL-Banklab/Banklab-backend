package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentType;
import org.apache.ibatis.annotations.Param;

/**
 * 사용자 투자 유형 정보를 조회, 삽입, 수정하는 매퍼 인터페이스
 */
public interface UserInvestmentTypeMapper {
    /**
     * 주어진 사용자 ID로 사용자 투자 유형 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return UserInvestmentType 객체
     */
    UserInvestmentType findByUserId(@Param("userId") Long userId);

    /**
     * 사용자 투자 유형 정보를 삽입합니다.
     * @param userInvestmentType 삽입할 UserInvestmentType 객체
     */
    void insertUserInvestmentType(UserInvestmentType userInvestmentType);
    /**
     * 사용자 투자 유형 정보를 수정합니다.
     * @param userInvestmentType 수정할 UserInvestmentType 객체
     */
    void updateUserInvestmentType(UserInvestmentType userInvestmentType);
}
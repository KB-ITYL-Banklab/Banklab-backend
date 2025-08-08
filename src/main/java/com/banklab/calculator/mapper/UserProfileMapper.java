package com.banklab.calculator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 사용자 프로필 매퍼 (계산기용)
 */
@Mapper
public interface UserProfileMapper {
    
    /**
     * 사용자 투자 프로필에서 계산기에 필요한 정보만 조회
     * @param userId 사용자 ID
     * @return 투자 가능 금액 범위와 투자 기간 범위
     */
    Map<String, String> findUserInvestmentInfoByUserId(@Param("userId") Long userId);
}

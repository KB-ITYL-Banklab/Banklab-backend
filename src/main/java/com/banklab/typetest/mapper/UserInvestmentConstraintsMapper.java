package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentConstraints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInvestmentConstraintsMapper {
    
    /**
     * 사용자의 활성 제약조건 목록 조회
     */
    List<UserInvestmentConstraints> findActiveConstraintsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 제약조건 저장
     */
    void insertUserInvestmentConstraints(UserInvestmentConstraints constraints);
    
    /**
     * 사용자의 모든 제약조건 비활성화
     */
    void deactivateAllConstraints(@Param("userId") Long userId);
}

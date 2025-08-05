package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.UserQuizResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 사용자 퀴즈 결과 매퍼
 */
@Mapper
public interface UserQuizResultMapper {
    
    /**
     * 사용자 퀴즈 결과 저장
     * @param userQuizResult 사용자 퀴즈 결과
     * @return 저장된 행 수
     */
    int insertUserQuizResult(UserQuizResultVO userQuizResult);
    
    /**
     * 사용자 최신 누적 포인트 조회
     * @param memberId 사용자 ID
     * @return 누적 포인트
     */
    Integer getLatestAccumulatedPoints(@Param("memberId") Long memberId);
    
    /**
     * 사용자 총 문제 풀이 수 조회
     * @param memberId 사용자 ID
     * @return 총 문제 풀이 수
     */
    Integer getTotalProblemCount(@Param("memberId") Long memberId);
    
    /**
     * 사용자 퀴즈 결과 업데이트
     * @param userQuizResult 업데이트할 결과
     * @return 업데이트된 행 수
     */
    int updateUserQuizResult(UserQuizResultVO userQuizResult);
}

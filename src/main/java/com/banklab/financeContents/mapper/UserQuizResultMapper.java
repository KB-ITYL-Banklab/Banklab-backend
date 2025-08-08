package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.UserQuizResultVO;
import com.banklab.financeContents.dto.UserQuizStatsDTO;
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
     * 사용자 총 정답 문제 수 조회
     * @param memberId 사용자 ID
     * @return 총 정답 문제 수
     */
    Integer getTotalCorrectProblemCount(@Param("memberId") Long memberId);
    
    /**
     * 사용자 퀴즈 결과 업데이트
     * @param userQuizResult 업데이트할 결과
     * @return 업데이트된 행 수
     */
    int updateUserQuizResult(UserQuizResultVO userQuizResult);
    
    /**
     * 사용자가 오늘 퀴즈를 이미 풀었는지 확인
     * @param memberId 사용자 ID
     * @return 오늘 풀이 횟수 (0이면 안 풀었음, 1이면 이미 풀었음)
     */
    int getTodayQuizCount(@Param("memberId") Long memberId);
    
    /**
     * 사용자의 오늘 퀴즈 결과 조회
     * @param memberId 사용자 ID
     * @return 오늘의 퀴즈 결과 (없으면 null)
     */
    UserQuizResultVO getTodayQuizResult(@Param("memberId") Long memberId);
    
    /**
     * 사용자 퀴즈 통계 조회 (누적 정답률 포함)
     * @param memberId 사용자 ID
     * @return 사용자 퀴즈 통계
     */
    UserQuizStatsDTO getUserQuizStats(@Param("memberId") Long memberId);
    
    /**
     * 사용자 퀴즈 결과 Upsert (Insert 또는 Update)
     * @param userQuizResult 사용자 퀴즈 결과
     * @return 저장/업데이트된 행 수
     */
    int upsertUserQuizResult(UserQuizResultVO userQuizResult);
    
    /**
     * 사용자 퀴즈 결과 조회 (member_id로)
     * @param memberId 사용자 ID
     * @return 사용자 퀴즈 결과 (없으면 null)
     */
    UserQuizResultVO getUserQuizResult(@Param("memberId") Long memberId);
}

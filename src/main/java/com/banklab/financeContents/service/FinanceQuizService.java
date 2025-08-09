package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.*;

import java.util.List;

// 경제 퀴즈 서비스 인터페이스
public interface FinanceQuizService {
    
    /**
     *
     *
     */
    // 모든 퀴즈 조회  @return 퀴즈 목록
    List<FinanceQuizDTO> getAllQuizzes();

    // 퀴즈 타입별 조회  @param quizType 퀴즈 타입  @return 퀴즈 목록
    List<FinanceQuizDTO> getQuizzesByType(String quizType);

    // ID로 퀴즈 조회  @param id 퀴즈 ID  @return 퀴즈 정보
    FinanceQuizDTO getQuizById(Integer id);

    // 랜덤 퀴즈 조회  @param count 가져올 퀴즈 개수  @return 랜덤 퀴즈 목록
    List<FinanceQuizDTO> getRandomQuizzes(int count);

    // 타입별 랜덤 퀴즈 조회  @param quizType 퀴즈 타입  @param count 가져올 퀴즈 개수  @return 랜덤 퀴즈 목록
    List<FinanceQuizDTO> getRandomQuizzesByType(String quizType, int count);

    // 퀴즈 답변 검증  @param answerRequest 답변 요청  @return 퀴즈 결과
    QuizResultDTO checkAnswer(QuizAnswerRequestDTO answerRequest);

    // 전체 퀴즈 개수 조회  @return 전체 퀴즈 개수
    int getTotalQuizCount();

    // 타입별 퀴즈 개수 조회  @param quizType 퀴즈 타입  @return 해당 타입의 퀴즈 개수
    int getQuizCountByType(String quizType);

    // 퀴즈 타입 목록 조회  @return 사용 가능한 퀴즈 타입 목록
    List<String> getAvailableQuizTypes();
    
    /**
     * 오늘의 퀴즈 3문제 조회
     * @return 오늘의 퀴즈 목록 (3문제)
     */
    List<FinanceQuizDTO> getTodayQuizzes();
    
    /**
     * 일일 퀴즈 결과 처리
     * @param request 퀴즈 답안 요청
     * @return 퀴즈 결과 및 포인트 정보
     */
    DailyQuizResultDTO processDailyQuizResults(DailyQuizRequestDTO request);
    
    /**
     * 사용자가 오늘 퀴즈를 이미 풀었는지 확인
     * @param memberId 사용자 ID
     * @return 오늘 풀이 여부 (true: 이미 풀었음, false: 아직 안 풀었음)
     */
    boolean hasUserSolvedTodayQuiz(Long memberId);
    
    /**
     * 사용자의 오늘 퀴즈 결과와 상세 정보 조회
     * @param memberId 사용자 ID
     * @return 오늘의 퀴즈 결과 및 상세 정보 (답안, 점수, 문제, 해설 포함)
     */
    DailyQuizResultDTO getTodayQuizResult(Long memberId);
    
    /**
     * 사용자 퀴즈 통계 조회 (누적 정답률 포함)
     * @param memberId 사용자 ID
     * @return 사용자 퀴즈 통계 (총 문제 수, 정답 수, 정답률, 총 포인트)
     */
    UserQuizStatsDTO getUserQuizStats(Long memberId);
}

package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.FinanceQuizVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


// 경제 퀴즈 매퍼 인터페이스
@Mapper
public interface FinanceQuizMapper {

    // 모든 퀴즈 조회  @return 퀴즈 목록
    List<FinanceQuizVO> findAllQuizzes();

    // 퀴즈 타입별 조회  @param quizType 퀴즈 타입 (객관식퀴즈, OX퀴즈 등)  @return 퀴즈 목록
    List<FinanceQuizVO> findQuizzesByType(@Param("quizType") String quizType);

    //ID로 퀴즈 조회  @param id 퀴즈 ID  @return 퀴즈 정보
    FinanceQuizVO findQuizById(@Param("id") Integer id);

    // 랜덤 퀴즈 조회  @param limit 가져올 퀴즈 개수  @return 랜덤 퀴즈 목록
    List<FinanceQuizVO> findRandomQuizzes(@Param("limit") int limit);

    // 타입별 랜덤 퀴즈 조회  @param quizType 퀴즈 타입  @param limit 가져올 퀴즈 개수  @return 랜덤 퀴즈 목록
    List<FinanceQuizVO> findRandomQuizzesByType(@Param("quizType") String quizType, @Param("limit") int limit);

    // 퀴즈 추가  @param quiz 퀴즈 정보  @return 성공 여부
    int insertQuiz(FinanceQuizVO quiz);

    // 퀴즈 수정  @param quiz 퀴즈 정보  @return 성공 여부
    int updateQuiz(FinanceQuizVO quiz);

    // 퀴즈 삭제  @param id 퀴즈 ID  @return 성공 여부
    int deleteQuiz(@Param("id") Integer id);

    // 전체 퀴즈 개수 조회  @return 전체 퀴즈 개수
    int getTotalQuizCount();

    // 타입별 퀴즈 개수 조회  @param quizType 퀴즈 타입  @return 해당 타입의 퀴즈 개수
    int getQuizCountByType(@Param("quizType") String quizType);
    
    /**
     * 인덱스 범위로 퀴즈 조회 (오늘의 퀴즈용)
     * @param startIndex 시작 인덱스
     * @param endIndex 끝 인덱스
     * @return 해당 범위의 퀴즈 목록
     */
    List<FinanceQuizVO> findQuizzesByIndexRange(@Param("startIndex") int startIndex, @Param("endIndex") int endIndex);
}

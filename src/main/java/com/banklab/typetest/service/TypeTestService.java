package com.banklab.typetest.service;

import com.banklab.typetest.domain.Question;
import com.banklab.typetest.dto.TypeTestResultDTO;

import java.util.List;
import java.util.Map;

public interface TypeTestService {
    /**
     * 모든 질문 목록을 반환합니다.
     *
     * @return 질문 리스트
     */
    List<Question> getAllQuestions();

    /**
     * 사용자의 답변을 제출하고 결과를 반환합니다.
     *
     * @param payload 사용자의 답변 데이터
     * @return 테스트 결과 DTO
     */
    TypeTestResultDTO submitAnswers(Map<String, Object> payload);
    /**
     * 사용자 ID로 테스트 결과를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 테스트 결과 DTO
     */
    TypeTestResultDTO getTestResultByUserId(Long userId);
}
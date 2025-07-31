package com.banklab.financeContents.dto;

import java.util.List;

/**
 * 퀴즈 응답을 위한 래퍼 DTO 클래스
 * 프론트엔드가 기대하는 {questions: [...]} 형식으로 응답을 제공합니다.
 */
public class QuizResponseDTO {
    private List<FinanceQuizDTO> questions;

    // 기본 생성자
    public QuizResponseDTO() {}

    // 생성자
    public QuizResponseDTO(List<FinanceQuizDTO> questions) {
        this.questions = questions;
    }

    // Getter & Setter
    public List<FinanceQuizDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<FinanceQuizDTO> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "QuizResponseDTO{" +
                "questions=" + questions +
                '}';
    }
}
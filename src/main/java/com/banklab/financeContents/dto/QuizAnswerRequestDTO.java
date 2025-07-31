package com.banklab.financeContents.dto;

/**
 * 퀴즈 답변 요청 DTO
 */
public class QuizAnswerRequestDTO {
    private Integer quizId;
    private String userAnswer;

    public QuizAnswerRequestDTO() {}

    public QuizAnswerRequestDTO(Integer quizId, String userAnswer) {
        this.quizId = quizId;
        this.userAnswer = userAnswer;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    @Override
    public String toString() {
        return "QuizAnswerRequestDTO{" +
                "quizId=" + quizId +
                ", userAnswer='" + userAnswer + '\'' +
                '}';
    }
}

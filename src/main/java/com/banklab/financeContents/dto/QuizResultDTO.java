package com.banklab.financeContents.dto;

/**
 * 퀴즈 결과 응답 DTO
 */
public class QuizResultDTO {
    private boolean correct;
    private String correctAnswer;
    private String explanation;
    private String userAnswer;

    public QuizResultDTO() {}

    public QuizResultDTO(boolean correct, String correctAnswer, String explanation, String userAnswer) {
        this.correct = correct;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    @Override
    public String toString() {
        return "QuizResultDTO{" +
                "correct=" + correct +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                ", userAnswer='" + userAnswer + '\'' +
                '}';
    }
}

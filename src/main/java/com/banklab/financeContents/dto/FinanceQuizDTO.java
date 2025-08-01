package com.banklab.financeContents.dto;

import java.util.List;

/**
 * 경제 퀴즈 DTO 클래스
 */
public class FinanceQuizDTO {
    private Integer id;
    private String quizType;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String answer;
    private String explanation;

    // 기본 생성자
    public FinanceQuizDTO() {}

    // 전체 필드 생성자
    public FinanceQuizDTO(Integer id, String quizType, String question, String option1, 
                         String option2, String option3, String option4, String answer, String explanation) {
        this.id = id;
        this.quizType = quizType;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.explanation = explanation;
    }

    // 선택지를 리스트로 반환하는 유틸리티 메서드
    public List<String> getOptions() {
        return List.of(
            option1 != null ? option1 : "",
            option2 != null ? option2 : "",
            option3 != null ? option3 : "",
            option4 != null ? option4 : ""
        ).stream()
         .filter(option -> !option.isEmpty())
         .toList();
    }

    // OX 퀴즈 여부 확인
    public boolean isOXQuiz() {
        return "OX퀴즈".equals(quizType);
    }

    // Getter & Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuizType() {
        return quizType;
    }

    public void setQuizType(String quizType) {
        this.quizType = quizType;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "FinanceQuizDTO{" +
                "id=" + id +
                ", quizType='" + quizType + '\'' +
                ", question='" + question + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", answer='" + answer + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}

package com.banklab.financeContents.domain;

/**
 * 경제 퀴즈 도메인 클래스
 */
public class FinanceQuizVO {
    private Integer id;          // 번호
    private String quizType;     // 구분 (객관식퀴즈, OX퀴즈 등)
    private String question;     // 문제내용
    private String option1;      // 보기1
    private String option2;      // 보기2
    private String option3;      // 보기3
    private String option4;      // 보기4
    private String answer;       // 정답
    private String explanation;  // 해설

    // 기본 생성자
    public FinanceQuizVO() {}

    // 전체 필드 생성자
    public FinanceQuizVO(Integer id, String quizType, String question, String option1, 
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
        return "FinanceQuizVO{" +
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

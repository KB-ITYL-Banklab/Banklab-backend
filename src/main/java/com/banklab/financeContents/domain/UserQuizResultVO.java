package com.banklab.financeContents.domain;

import java.time.LocalDateTime;

/**
 * 사용자 퀴즈 결과 VO (user_quiz_results 테이블 매핑)
 */
public class UserQuizResultVO {
    private Long id;
    private Long memberId;
    private String userAnswer;
    private Integer problem;
    private Integer accumulatedPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserQuizResultVO() {}

    public UserQuizResultVO(Long id, Long memberId, String userAnswer, Integer problem, 
                          Integer accumulatedPoints, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.userAnswer = userAnswer;
        this.problem = problem;
        this.accumulatedPoints = accumulatedPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public Integer getProblem() {
        return problem;
    }

    public void setProblem(Integer problem) {
        this.problem = problem;
    }

    public Integer getAccumulatedPoints() {
        return accumulatedPoints;
    }

    public void setAccumulatedPoints(Integer accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserQuizResultVO{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", userAnswer='" + userAnswer + '\'' +
                ", problem=" + problem +
                ", accumulatedPoints=" + accumulatedPoints +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

package com.banklab.financeContents.dto;

import java.time.LocalDateTime;

/**
 * 사용자 퀴즈 결과 DTO (user_quiz_results 테이블 맞춤)
 */
public class UserQuizResultDTO {
    private Long id;
    private String memberId;
    private String userAnswer;
    private Integer problem;
    private Integer accumulatedPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserQuizResultDTO() {}

    public UserQuizResultDTO(Long id, String memberId, String userAnswer, Integer problem, 
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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
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
        return "UserQuizResultDTO{" +
                "id=" + id +
                ", memberId='" + memberId + '\'' +
                ", userAnswer='" + userAnswer + '\'' +
                ", problem=" + problem +
                ", accumulatedPoints=" + accumulatedPoints +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

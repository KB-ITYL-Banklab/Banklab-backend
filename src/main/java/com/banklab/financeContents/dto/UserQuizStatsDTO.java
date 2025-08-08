package com.banklab.financeContents.dto;

/**
 * 사용자 퀴즈 통계 DTO
 */
public class UserQuizStatsDTO {
    private Long memberId;
    private Integer totalProblems;
    private Integer correctProblems;
    private Double accuracyRate;
    private Integer totalPoints;

    public UserQuizStatsDTO() {}

    public UserQuizStatsDTO(Long memberId, Integer totalProblems, Integer correctProblems, 
                          Double accuracyRate, Integer totalPoints) {
        this.memberId = memberId;
        this.totalProblems = totalProblems;
        this.correctProblems = correctProblems;
        this.accuracyRate = accuracyRate;
        this.totalPoints = totalPoints;
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Integer getTotalProblems() {
        return totalProblems;
    }

    public void setTotalProblems(Integer totalProblems) {
        this.totalProblems = totalProblems;
    }

    public Integer getCorrectProblems() {
        return correctProblems;
    }

    public void setCorrectProblems(Integer correctProblems) {
        this.correctProblems = correctProblems;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    @Override
    public String toString() {
        return "UserQuizStatsDTO{" +
                "memberId=" + memberId +
                ", totalProblems=" + totalProblems +
                ", correctProblems=" + correctProblems +
                ", accuracyRate=" + accuracyRate +
                ", totalPoints=" + totalPoints +
                '}';
    }
}

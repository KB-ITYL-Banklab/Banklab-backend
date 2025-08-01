package com.banklab.financeContents.dto;

import java.util.List;

/**
 * 일일 퀴즈 결과 DTO
 */
public class DailyQuizResultDTO {
    private int correctCount;        // 맞힌 문제 수
    private int totalQuestions;      // 전체 문제 수
    private int earnedPoints;       // 이번에 획득한 포인트
    private int totalAccumulatedPoints; // 총 누적 포인트
    private List<QuizResultDTO> detailResults; // 문제별 상세 결과

    public DailyQuizResultDTO() {}

    public DailyQuizResultDTO(int correctCount, int totalQuestions, int earnedPoints, 
                             int totalAccumulatedPoints, List<QuizResultDTO> detailResults) {
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.earnedPoints = earnedPoints;
        this.totalAccumulatedPoints = totalAccumulatedPoints;
        this.detailResults = detailResults;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public int getTotalAccumulatedPoints() {
        return totalAccumulatedPoints;
    }

    public void setTotalAccumulatedPoints(int totalAccumulatedPoints) {
        this.totalAccumulatedPoints = totalAccumulatedPoints;
    }

    public List<QuizResultDTO> getDetailResults() {
        return detailResults;
    }

    public void setDetailResults(List<QuizResultDTO> detailResults) {
        this.detailResults = detailResults;
    }

    @Override
    public String toString() {
        return "DailyQuizResultDTO{" +
                "correctCount=" + correctCount +
                ", totalQuestions=" + totalQuestions +
                ", earnedPoints=" + earnedPoints +
                ", totalAccumulatedPoints=" + totalAccumulatedPoints +
                ", detailResults=" + detailResults +
                '}';
    }
}

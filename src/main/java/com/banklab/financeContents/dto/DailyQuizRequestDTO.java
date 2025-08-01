package com.banklab.financeContents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 일일 퀴즈 요청 DTO (현재 테이블 구조에 맞춘 단순화 버전)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyQuizRequestDTO {
    private String userAnswer; // 3문제의 답안을 하나의 문자열로 (예: "1O2")
}

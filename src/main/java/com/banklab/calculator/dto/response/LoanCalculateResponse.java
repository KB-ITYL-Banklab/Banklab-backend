package com.banklab.calculator.dto.response;

import com.banklab.calculator.domain.LoanType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 대출 계산 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCalculateResponse {
    
    // 입력 조건
    private InputConditions inputConditions;
    
    // 계산 결과
    private CalculationResults results;
    
    // 상환 스케줄 (상세보기용)
    private List<LoanScheduleItem> schedule;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InputConditions {
        private Long loanAmount;              // 대출금액
        private Integer loanTermMonths;       // 대출기간 (개월)
        private Double loanRate;              // 대출금리 (%)
        private Integer gracePeriodMonths;    // 거치기간 (개월)
        private LoanType repaymentMethod;     // 상환방법
        private String repaymentMethodName;   // 상환방법명
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalculationResults {
        private Long loanAmount;              // 대출금액
        private Long totalInterest;           // 총이자
        private Long totalCost;               // 총비용 (대출금액 + 총이자)
        private Long avgMonthlyPrincipal;     // 월 평균 상환 원금
        private Long avgMonthlyInterest;      // 월 평균 이자액
        private Long avgMonthlyPayment;       // 월 평균 납입금
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoanScheduleItem {
        private Integer month;      // 회차
        private String period;      // 기간 구분 (거치기간/상환기간)
        private Long payment;       // 상환금액
        private Long principal;     // 원금
        private Long interest;      // 이자
        private Long balance;       // 잔액
    }
}

package com.banklab.calculator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnuityCalculateResponse {
    private InputConditions inputConditions;
    private AnnuityResults results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InputConditions {
        private Long monthlySaving;
        private Long lumpSum;
        private Integer savingYears;
        private Double rate;
        private Integer paymentYears;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnnuityResults {
        private Long totalPayout;
        private Integer totalMonths;
        private Long monthlyPayout;
    }
}


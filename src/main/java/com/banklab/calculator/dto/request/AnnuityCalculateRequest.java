package com.banklab.calculator.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnuityCalculateRequest {
    private Long monthlySaving;     // 월 적립금
    private Long lumpSum;           // 거치금
    private Integer savingYears;    // 투자기간 (연 단위)
    private Double rate;            // 연 이율 (%)
    private Integer paymentYears;   // 연금 수령기간 (연 단위)
}

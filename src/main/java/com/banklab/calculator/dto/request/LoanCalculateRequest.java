package com.banklab.calculator.dto.request;

import com.banklab.calculator.domain.LoanType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * 대출 계산 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanCalculateRequest {

    private Long loanAmount; // 대출금액

    private Integer loanTermMonths; // 대출기간 (개월)

    private Double loanRate; // 대출금리 (%)

    private Integer gracePeriodMonths = 0; // 거치기간 (개월, 기본값: 0)

    private LoanType repaymentMethod = LoanType.EQUAL_PAYMENT; // 상환방법 (기본값: 원리금균등)
}

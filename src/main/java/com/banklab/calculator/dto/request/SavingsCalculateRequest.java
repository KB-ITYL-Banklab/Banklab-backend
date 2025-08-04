package com.banklab.calculator.dto.request;

import com.banklab.calculator.domain.SavingsType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * 적금 계산 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsCalculateRequest {

    // 만기로 월 금액 입력할경우
    private Long monthlyPayment; // 월 납입금

    // 목표 금액으로 월 납입금 계산하는 경우
    private Long targetAmount; // 목표 금액 (optional)

    private Integer termMonths; // 기간 (개월)

    private Double rate; // 연이율 (%)

    private Boolean isCompound = true; // 복리 여부 (기본값: 복리)

    private SavingsType savingsType = SavingsType.MATURITY_AMOUNT; // 적금 유형 (기본값 : 만기)


}

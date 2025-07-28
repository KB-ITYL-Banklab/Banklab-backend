package com.banklab.typetest.domain;

import com.banklab.typetest.domain.enums.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvestmentProfile {
    private Long id;
    private Long userId; // 사용자 ID
    private AmountRange availableAmountRange; // 사용자가 투자 가능한 금액 범위
    private ReturnRange targetReturnRange; // 사용자가 목표로 하는 수익률 범위
    private PeriodRange investmentPeriodRange; // 사용자가 희망하는 투자 기간 범위
    private RiskRange lossToleranceRange; // 사용자가 감내할 수 있는 손실 허용 범위(리스크)
    private InvestmentStyle investmentStyle; // 사용자의 투자 성향(예: 일시금, 분할납부 등)
    private Priority priority; // 투자 시 우선순위(예: 안정성, 수익 등)
}

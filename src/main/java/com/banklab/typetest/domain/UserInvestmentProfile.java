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
    private Long userId;
    private AmountRange availableAmountRange;
    private ReturnRange targetReturnRange;
    private PeriodRange investmentPeriodRange;
    private RiskRange lossToleranceRange;
    private InvestmentStyle investmentStyle;
    private Priority priority;
}


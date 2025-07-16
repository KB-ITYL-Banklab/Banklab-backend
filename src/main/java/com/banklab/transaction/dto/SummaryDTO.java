package com.banklab.transaction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SummaryDTO {
    private MonthlySummaryDTO monthlySummary;
    private List<DailyExpenseDTO> dailyExpense;
    private List<WeeklyExpenseDTO> weeklyExpense;
}

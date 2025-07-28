package com.banklab.transaction.dto.response;

import com.banklab.category.dto.CategoryExpenseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccountSummaryDTO {
    private String account;
    private MonthlySummaryDTO monthlySummary;
    private List<DailyExpenseDTO> dailyExpense;
    private List<WeeklyExpenseDTO> weeklyExpense;
    private List<CategoryExpenseDTO> categoryExpense;
}

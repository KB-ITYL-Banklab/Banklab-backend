package com.banklab.transaction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySummaryDTO {
    private int year;
    private int month;
    private long totalIncome;
    private long totalExpense;
}

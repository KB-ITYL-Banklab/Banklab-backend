package com.banklab.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MonthlySummaryDTO {
    private long totalIncome;
    private long totalExpense;
}

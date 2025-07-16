package com.banklab.transaction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class WeeklyExpenseDTO {
    private int weekNumber;
    private Date startDate;
    private Date endDate;
    private long totalExpense;
}

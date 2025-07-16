package com.banklab.transaction.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class WeeklyExpenseDTO {
    private int weekNumber;
    private Date startDate;
    private Date endDate;
    private long totalExpense;
}

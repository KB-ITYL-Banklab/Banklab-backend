package com.banklab.transaction.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
public class DailyExpenseDTO {
    private Date date;
    private long totalExpense;
}

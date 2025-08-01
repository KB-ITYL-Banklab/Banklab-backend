package com.banklab.transaction.summary.dto;

import java.util.Date;

public class DailySummaryDTO {
    private Long memberId;
    private Long categoryId; // 0 = 전체
    private Date date;
    private Long totalExpense;
    private Long totalIncome;
    private String resAccount;
}

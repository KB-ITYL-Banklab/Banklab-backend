package com.banklab.transaction.summary.service;

import com.banklab.account.domain.AccountVO;

import java.time.LocalDate;
import java.util.Date;


public interface SummaryBatchService {
    void aggregateDailySummary(LocalDate targetDate);
    void initDailySummary(Long memberId, AccountVO account);
}

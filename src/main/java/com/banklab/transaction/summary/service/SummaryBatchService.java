package com.banklab.transaction.summary.service;

import org.springframework.stereotype.Service;

import java.util.Date;


public interface SummaryBatchService {
    void aggregateDailySummary(Date targetDate);
}

package com.banklab.transaction.summary.mapper;

import com.banklab.transaction.summary.dto.DailySummaryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface SummaryMapper {
    void upsertDailySummary(DailySummaryDTO dailySummary);
    Date getLastTransactionDate(@Param("memberId") Long memberId);
}

package com.banklab.transaction.summary.mapper;

import com.banklab.transaction.summary.dto.DailySummaryDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface SummaryMapper {
    LocalDate getLastSummaryDate(@Param("memberId") Long memberId, @Param("resAccount") String resAccount);
    List<DailySummaryDTO> getDailySummary(@Param("memberId") Long memberId, @Param("targetDate") Date date);
    void upsertDailySummary(DailySummaryDTO dailySummary);
    void batchUpsertDailySummary(List<DailySummaryDTO> dailySummaryList);

}

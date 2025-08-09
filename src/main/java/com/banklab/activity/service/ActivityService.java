package com.banklab.activity.service;

import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;

public interface ActivityService {
    void saveContentViewLog(Long memberId, ContentViewLogDTO dto);
    void saveCompareUsageLog(Long memberId);
    void saveReportViewLog(Long memberId, ReportViewLogDTO dto);

    int countAllContentView(Long memberId);
    int countAllCompareUsage(Long memberId);
    int countAllSpendingReportView(Long memberId);
    int countTodayContentView(Long memberId);
    int countThisWeekCompareUsage(Long memberId);
    int countThisMonthSpendingViews(Long memberId);
    boolean hasRecentMyDataLog(Long memberId);
    boolean hasRecentContentLog(Long memberId);
}

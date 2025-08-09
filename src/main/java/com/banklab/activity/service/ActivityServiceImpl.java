package com.banklab.activity.service;

import com.banklab.activity.domain.CompareUsageLogVO;
import com.banklab.activity.domain.ContentViewLogVO;
import com.banklab.activity.domain.SpendingReportViewLogVO;
import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;
import com.banklab.activity.mapper.CompareUsageLogMapper;
import com.banklab.activity.mapper.ContentViewLogMapper;
import com.banklab.activity.mapper.MyDataLogMapper;
import com.banklab.activity.mapper.SpendingReportViewLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ContentViewLogMapper contentViewLogMapper;
    private final CompareUsageLogMapper compareUsageLogMapper;
    private final SpendingReportViewLogMapper spendingReportViewLogMapper;
    private final MyDataLogMapper myDataLogMapper;

    @Transactional
    @Override
    public void saveContentViewLog(Long memberId, ContentViewLogDTO dto) {
        ContentViewLogVO contentViewLog = dto.toVO(memberId);
        contentViewLogMapper.insert(contentViewLog);
    }

    @Transactional
    @Override
    public void saveCompareUsageLog(Long memberId) {
        CompareUsageLogVO compareUsageLog = CompareUsageLogVO.builder()
                .memberId(memberId)
                .build();
        compareUsageLogMapper.insert(compareUsageLog);
    }

    @Transactional
    @Override
    public void saveReportViewLog(Long memberId, ReportViewLogDTO dto) {
        SpendingReportViewLogVO spendingReportViewLog = dto.toVO(memberId);
        spendingReportViewLogMapper.insert(spendingReportViewLog);
    }

    @Override
    public int countAllContentView(Long memberId) {
        return contentViewLogMapper.countByMember(memberId);
    }

    @Override
    public int countAllCompareUsage(Long memberId) {
        return compareUsageLogMapper.countByMember(memberId);
    }

    @Override
    public int countAllSpendingReportView(Long memberId) {
        return spendingReportViewLogMapper.countByMember(memberId);
    }

    @Override
    public int countTodayContentView(Long memberId) {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();           // 오늘 00:00:00
        LocalDateTime end = today.atTime(LocalTime.MAX);      // 오늘 23:59:59.999...
        return contentViewLogMapper.countByDateRange(memberId, start, end);
    }

    @Override
    public int countThisWeekCompareUsage(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = sunday.atTime(LocalTime.MAX);

        return compareUsageLogMapper.countByDateRange(memberId, start, end);
    }

    @Override
    public int countThisMonthSpendingViews(Long memberId) {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay(); // 이번 달 1일 00:00
        LocalDateTime end = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX); // 이번 달 마지막날 23:59:59

        return spendingReportViewLogMapper.countByDateRange(memberId, start, end);
    }

    @Override
    public boolean hasRecentMyDataLog(Long memberId) {
        return myDataLogMapper.hasRecentFetch(memberId);
    }

    @Override
    public boolean hasRecentContentLog(Long memberId) {
        return contentViewLogMapper.hasRecentView(memberId);
    }
}

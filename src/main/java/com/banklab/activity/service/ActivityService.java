package com.banklab.activity.service;

import com.banklab.activity.domain.EventType;
import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;
import com.banklab.common.util.Periods;
import com.banklab.mission.domain.MissionCycle;

import java.time.LocalDate;

public interface ActivityService {
    // 쓰기(변경 없음)
    void saveContentViewLog(Long memberId, ContentViewLogDTO dto);
    void saveCompareUsageLog(Long memberId);
    void saveReportViewLog(Long memberId, ReportViewLogDTO dto);
    void saveMyDataFetch(Long memberId);

    // 읽기(통일)
    int countInRange(Long memberId, EventType key, LocalDate startInclusive, LocalDate endInclusive);

    // 최근 N일 / 오늘 / 이번주 / 이번달
    default int countLastNDays(Long memberId, EventType key, int recentDaysKst) {
        LocalDate start = Periods.recentStart(recentDaysKst);
        return countInRange(memberId, key, start, Periods.today());
    }
    default int countToday(Long memberId, EventType key) {
        return countInRange(memberId, key, Periods.today(), Periods.today());
    }
    default int countThisWeek(Long memberId, EventType key) {
        LocalDate start = Periods.periodStart(MissionCycle.WEEKLY);
        return countInRange(memberId, key, start, Periods.today());
    }
    default int countThisMonth(Long memberId, EventType key) {
        LocalDate start = Periods.periodStart(MissionCycle.MONTHLY);
        return countInRange(memberId, key, start, Periods.today());
    }
    default int countAll(Long memberId, EventType key) {
        return countInRange(memberId, key,
                LocalDate.of(1970,1,1), Periods.today());
    }

    // 기존 불리언들도 통일 메서드로 대체 가능
    default boolean hasRecent(Long memberId, EventType key, int recentDaysKst) {
        return countLastNDays(memberId, key, recentDaysKst) > 0;
    }
}

package com.banklab.activity.service;

import com.banklab.activity.domain.EventType;
import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;
import com.banklab.common.util.Periods;
import com.banklab.mission.domain.MissionCycle;

import java.time.LocalDate;

public interface ActivityService {
    /**
     * 경제 컨텐츠 열람 로그 저장
     * @param memberId
     * @param dto
     */
    void saveContentViewLog(Long memberId, ContentViewLogDTO dto);

    /**
     * 금융 상품 계산기 사용 로그 저장
     * @param memberId
     */
    void saveCompareUsageLog(Long memberId);

    /**
     * 분석 열람 로그 저장
     * @param memberId
     * @param dto
     */
    void saveReportViewLog(Long memberId, ReportViewLogDTO dto);

    /**
     * 마이데이터 열람 로그 저장
     * @param memberId
     */
    void saveMyDataFetch(Long memberId);

    /**
     * 기간 내 로그 횟수 계산
     * @param memberId
     * @param key
     * @param startInclusive
     * @param endInclusive
     * @return
     */
    int countInRange(Long memberId, EventType key, LocalDate startInclusive, LocalDate endInclusive);

    // 최근 N일 / 오늘 / 이번주 / 이번달 로그 횟수 계산
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

    /**
     * 누적 로그 횟수 계산
     * @param memberId
     * @param key
     * @return
     */
    default int countAll(Long memberId, EventType key) {
        return countInRange(memberId, key,
                LocalDate.of(1970,1,1), Periods.today());
    }

    /**
     * 최근 N일 내 활동했는지 확인
     * @param memberId
     * @param key
     * @param recentDaysKst
     * @return
     */
    default boolean hasRecent(Long memberId, EventType key, int recentDaysKst) {
        return countLastNDays(memberId, key, recentDaysKst) > 0;
    }
}

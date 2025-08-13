package com.banklab.common.util;

import com.banklab.mission.domain.MissionCycle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public final class Periods {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static LocalDate today() { return LocalDate.now(KST); }

    public static String yyyymmdd(LocalDate d){ return d.format(DateTimeFormatter.BASIC_ISO_DATE); }

    public static LocalDate periodStart(MissionCycle cycle) {
        LocalDate t = today();
        return switch (cycle) {
            case DAILY -> t;
            case WEEKLY -> t.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> t.withDayOfMonth(1);
            default -> LocalDate.of(1970,1,1); // NONE
        };
    }

    // recentDays: 오늘 포함 N일 → start = today - (N-1)
    public static LocalDate recentStart(int recentDays) {
        return today().minusDays(recentDays - 1L);
    }
}


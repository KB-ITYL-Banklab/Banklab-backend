package com.banklab.activity.util;

import com.banklab.activity.domain.ContentType;
import com.banklab.common.util.Periods;

import java.time.LocalDate;

public final class IdemKeys {
    // 하루에 동일한 컨텐츠 열람 로그 방지
    public static String dailyContentView(Long memberId, ContentType contentType, String contentKey, LocalDate date) {
        return memberId + ":CONTENT_VIEW:" + contentType + ":" + contentKey + ":" + Periods.yyyymmdd(date);
    }
    // 하루에 동일한 월 리포트 열람 로그 방지
    public static String dailyReportView(Long memberId, String reportMonth, LocalDate date){
        return memberId + ":SPENDING_REPORT_VIEW:" + reportMonth + ":" + Periods.yyyymmdd(date);
    }
    // 하루에 동일한 마이데이터 열람 로그 방지
    public static String dailyMydata(Long memberId, LocalDate date){
        return memberId + ":MYDATA_FETCH:" + Periods.yyyymmdd(date);
    }
}

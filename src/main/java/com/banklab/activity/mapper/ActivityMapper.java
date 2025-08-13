package com.banklab.activity.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface ActivityMapper {
    // 이벤트 적재(멱등)
    int insertEvent(@Param("memberId") Long memberId,
                    @Param("eventType") String eventType,
                    @Param("payloadJson") String payloadJson,
                    @Param("dedupKey") String dedupKey);

    // 일별 집계 upsert
    int upsertDailyAgg(@Param("memberId") Long memberId,
                       @Param("activityDate") LocalDate activityDate,
                       @Param("keyName") String keyName,
                       @Param("cnt") int cnt);

    Integer sumCount(@Param("memberId") Long memberId,
                     @Param("keyName") String keyName,
                     @Param("start") LocalDate start,
                     @Param("end") LocalDate end);

    // 보상 멱등
    int insertExpGrant(@Param("memberId") Long memberId,
                       @Param("missionId") Integer missionId,
                       @Param("exp") Integer exp);
}

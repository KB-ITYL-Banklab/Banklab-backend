package com.banklab.activity.service;

import com.banklab.activity.domain.EventType;
import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;
import com.banklab.activity.mapper.ActivityMapper;
import com.banklab.activity.util.IdemKeys;
import com.banklab.common.util.Periods;
import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.event.MissionTriggerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper mapper;
    private final ApplicationEventPublisher publisher;

    private final ObjectMapper om = new ObjectMapper();

    // ========== 쓰기(행동 발생) ==========
    @Transactional
    @Override
    public void saveContentViewLog(Long memberId, ContentViewLogDTO dto) {
        LocalDate today = Periods.today();
        String idem = IdemKeys.dailyContentView(memberId, dto.getContentType(), dto.getContentKey(), today);

        String payload = toJsonSafe(dto); // 타입/키 기록용
        int inserted = mapper.insertEvent(memberId, "CONTENT_VIEW", payload, idem);
        if (inserted == 0) return; // 오늘 이미 인정됨 → 종료

        mapper.upsertDailyAgg(memberId, today, "CONTENT_VIEW", 1);

        // (선택) 여기서 바로 미션 트리거
        publisher.publishEvent(
                new MissionTriggerEvent(memberId, ConditionKey.CONTENT_VIEW_COUNT, today)
        );
        publisher.publishEvent(
                new MissionTriggerEvent(memberId, ConditionKey.RECENT_FINANCIAL_ACTIVITY, today)
        );
    }

    @Transactional
    @Override
    public void saveCompareUsageLog(Long memberId) {
        LocalDate today = Periods.today();
        // 비교 기능은 중복 허용(멱등키 생략 가능): dedupKey = null
        int inserted = mapper.insertEvent(memberId, "COMPARE_USAGE", null, null);
        if (inserted >= 0) { // 항상 upsert
            mapper.upsertDailyAgg(memberId, today, "COMPARE_USAGE", 1);
            publisher.publishEvent(
                    new MissionTriggerEvent(memberId, ConditionKey.COMPARE_USAGE_COUNT, today)
            );
        }
    }

    @Transactional
    @Override
    public void saveReportViewLog(Long memberId, ReportViewLogDTO dto) {
        LocalDate today = Periods.today();
        String idem = IdemKeys.dailyReportView(memberId, dto.getReportMonth(), today);
        String payload = toJsonSafe(dto);

        int inserted = mapper.insertEvent(memberId, "SPENDING_REPORT_VIEW", payload, idem);
        if (inserted == 0) return;

        mapper.upsertDailyAgg(memberId, today, "SPENDING_REPORT_VIEW", 1);
        publisher.publishEvent(
                new MissionTriggerEvent(memberId, ConditionKey.SPENDING_REPORT_VIEW_COUNT, today)
        );
    }

    @Transactional
    @Override
    public void saveMyDataFetch(Long memberId) {
        LocalDate today = Periods.today();
        String idem = IdemKeys.dailyMydata(memberId, today);
//        String payload = "{\"fetchType\":\"" + fetchType + "\"}";

        int inserted = mapper.insertEvent(memberId, "MYDATA_FETCH", null, idem);
        if (inserted == 0) return;

        mapper.upsertDailyAgg(memberId, today, "MYDATA_FETCH", 1);
        publisher.publishEvent(
                new MissionTriggerEvent(memberId, ConditionKey.MYDATA_FETCHED_RECENTLY, today)
        );
    }

    // ========== 읽기(카운트 API) : 모두 집계에서 합산 ==========
    @Override
    public int countInRange(Long memberId, EventType key, LocalDate startInclusive, LocalDate endInclusive) {
        return mapper.sumCount(memberId, key.name(), startInclusive, endInclusive);
    }

    private String toJsonSafe(Object o){
        try { return om.writeValueAsString(o); }
        catch (Exception e){ return null; }
    }
}

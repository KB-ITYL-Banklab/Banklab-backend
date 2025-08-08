package com.banklab.mission.evaluator;

import com.banklab.activity.service.ActivityService;
import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.domain.MissionCycle;
import com.banklab.mission.domain.MissionVO;
import com.banklab.typetest.service.TypeTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CountMissionEvaluator implements MissionEvaluator {

    private final TypeTestService typeTestService;
    private final ActivityService activityService;

    @Override
    public Set<ConditionKey> supportedKeys() {
        return EnumSet.of(
                ConditionKey.TYPE_TEST_COUNT,
                ConditionKey.CONTENT_VIEW_COUNT,
                ConditionKey.COMPARE_USAGE_COUNT,
                ConditionKey.SPENDING_REPORT_VIEW_COUNT
        );
    }

    @Override
    public int evaluate(Long memberId, MissionVO mission) {
        ConditionKey key = mission.getConditionKey();
        MissionCycle cycle = mission.getMissionCycle();

        return switch (key) {
            case TYPE_TEST_COUNT -> {
                Integer count = typeTestService.getUserInvestmentType(memberId).getCumulativeViews();
                if (count == null) yield 0;
                yield count;
            }
            case CONTENT_VIEW_COUNT -> {
                if (cycle.equals(MissionCycle.DAILY)) {
                    yield activityService.countTodayContentView(memberId);
                }
                yield activityService.countAllContentView(memberId);
            }
            case COMPARE_USAGE_COUNT -> {
                if (cycle.equals(MissionCycle.WEEKLY)) {
                    yield activityService.countThisWeekCompareUsage(memberId);
                }
                yield activityService.countAllCompareUsage(memberId);
            }
            case SPENDING_REPORT_VIEW_COUNT -> activityService.countAllSpendingReportView(memberId);
            default -> throw new UnsupportedOperationException("Unknown key: " + key);
        };
    }
}

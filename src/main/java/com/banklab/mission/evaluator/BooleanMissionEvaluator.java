package com.banklab.mission.evaluator;

import com.banklab.activity.service.ActivityService;
import com.banklab.financeContents.service.FinanceQuizService;
import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.domain.MissionVO;
import com.banklab.typetest.service.TypeTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BooleanMissionEvaluator implements MissionEvaluator {

    private final FinanceQuizService financeQuizService;
    private final ActivityService activityService;
    private final TypeTestService typeTestService;

    @Override
    public Set<ConditionKey> supportedKeys() {
        return EnumSet.of(
                ConditionKey.DAILY_QUIZ_SOLVED,
                ConditionKey.MYDATA_FETCHED_RECENTLY,
                ConditionKey.RECENT_FINANCIAL_ACTIVITY,
                ConditionKey.TYPE_TEST_RECENT
        );
    }

    @Override
    public int evaluate(Long memberId, MissionVO mission) {
        ConditionKey key = mission.getConditionKey();

        return switch (key) {
            case DAILY_QUIZ_SOLVED -> financeQuizService.hasUserSolvedTodayQuiz(memberId) ? 1 : 0;
            case MYDATA_FETCHED_RECENTLY -> activityService.hasRecentMyDataLog(memberId) ? 1 : 0;
            case RECENT_FINANCIAL_ACTIVITY -> activityService.hasRecentContentLog(memberId) ? 1 : 0;
            case TYPE_TEST_RECENT -> isWithin60Days(memberId) ? 1 : 0;
            default -> throw new UnsupportedOperationException("Unknown key: " + key);
        };
    }

    private boolean isWithin60Days(Long memberId) {
        String updatedAt = typeTestService.getUserInvestmentType(memberId).getUpdatedAt();
        if (updatedAt == null || updatedAt.isEmpty()) {
            return false; // 날짜 없음
        }

        // 날짜 형식에 맞춰 포맷터 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate updatedDate = LocalDate.parse(updatedAt, formatter);

        long daysBetween = ChronoUnit.DAYS.between(updatedDate, LocalDate.now());
        return daysBetween <= 60;
    }
}

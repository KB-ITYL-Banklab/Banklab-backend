package com.banklab.mission.evaluator;

import com.banklab.financeContents.dto.DailyQuizResultDTO;
import com.banklab.financeContents.service.FinanceQuizService;
import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.domain.MissionVO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RatioMissionEvaluator implements MissionEvaluator {

    private final FinanceQuizService financeQuizService;
    private final TransactionService transactionService;

    @Override
    public Set<ConditionKey> supportedKeys() {
        return EnumSet.of(
                ConditionKey.QUIZ_SUCCESS_RATE,
                ConditionKey.SPENDING_RATIO_DECREASED
        );
    }

    @Override
    public int evaluate(Long memberId, MissionVO mission) {
        ConditionKey key = mission.getConditionKey();

        return switch (key) {
            case QUIZ_SUCCESS_RATE -> calculateAccuracy(memberId);
            case SPENDING_RATIO_DECREASED -> spendRateReduced(memberId);
            default -> throw new UnsupportedOperationException("Unknown key: " + key);
        };
    }

    private int calculateAccuracy(Long memberId) {
        DailyQuizResultDTO quiz = financeQuizService.getTodayQuizResult(memberId);
        int correctCount = quiz.getCorrectCount();
        int totalCount = quiz.getTotalQuestions();
        if (totalCount == 0) {
            return 0; // 나눗셈 방지
        }

        return (int) (((double) correctCount / totalCount) * 100.0);
    }

    private int spendRateReduced(Long memberId) {
        // 최근 완료된 달과 그 전달 구하기
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        YearMonth prevMonth = lastMonth.minusMonths(1);

        // lastMonth 기간
        Date lastStart = toDate(lastMonth.atDay(1).atStartOfDay());
        Date lastEnd = toDate(lastMonth.plusMonths(1).atDay(1).atStartOfDay());

        // prevMonth 기간
        Date prevStart = toDate(prevMonth.atDay(1).atStartOfDay());
        Date prevEnd = toDate(prevMonth.plusMonths(1).atDay(1).atStartOfDay());

        // 요약 정보 가져오기
        MonthlySummaryDTO lastSummary = transactionService.getMonthlySummary(memberId, lastStart, lastEnd);
        MonthlySummaryDTO prevSummary = transactionService.getMonthlySummary(memberId, prevStart, prevEnd);

        long lastIncome = lastSummary.getTotalIncome();
        long lastExpense = lastSummary.getTotalExpense();
        long prevIncome = prevSummary.getTotalIncome();
        long prevExpense = prevSummary.getTotalExpense();

        // 소득이 0 이하이면 계산 불가
        if (prevIncome <= 0 || lastIncome <= 0) {
            return 0;
        }

        // 소비율 계산 (double로 변환하여 소수점 계산)
        double lastRate = (double) lastExpense / lastIncome;
        double prevRate = (double) prevExpense / prevIncome;

        return (int) (((prevRate - lastRate) / prevRate) * 100);
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}

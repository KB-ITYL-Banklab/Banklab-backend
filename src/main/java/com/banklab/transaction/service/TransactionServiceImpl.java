package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.*;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public void saveTransactionList(Long memberId,AccountVO account, List<TransactionHistoryVO> transactions) {
        if(transactions.isEmpty())  return;
        for(TransactionHistoryVO t: transactions){
            transactionMapper.saveTransaction(t);
        }
    }

    @Override
    public LocalDate getLastTransactionDay(Long memberId, String account) {
        return transactionMapper.getLastTransactionDate(memberId, account);
    }

    @Transactional
    public void updateCategories(List<TransactionHistoryVO> transactions){
        transactionMapper.updateCategories(transactions);
    }


    /**
     * @param memberId 사용자 id
     * @param startDate 시작일
     * @param endDate   종료일*
     * @return 월, 주간, 일간 내역 리스트
     */
    @Override
    public SummaryDTO getSummary(Long memberId, Date startDate, Date endDate) {
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }

        MonthlySummaryDTO monthlySummary = getMonthlySummary(memberId, startDate, endDate);
        List<DailyExpenseDTO> dailyExpense = getDailyExpense(memberId, startDate, endDate);
        List<WeeklyExpenseDTO> weeklyExpense = getWeeklyExpense(dailyExpense, startDate, endDate);
        List<CategoryExpenseDTO> categoryExpense = getCategoryExpense(memberId, startDate, endDate);

        AccountSummaryDTO summary = AccountSummaryDTO.builder()
                .account("Total") // Aggregated data for the member
                .monthlySummary(monthlySummary)
                .dailyExpense(dailyExpense)
                .weeklyExpense(weeklyExpense)
                .categoryExpense(categoryExpense)
                .build();

        List<AccountSummaryDTO> accountSummaries = Collections.singletonList(summary);

        return SummaryDTO.builder()
                .accountSummaries(accountSummaries)
                .build();
    }

    /**
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 해당 월 총수입 & 지출
     */
    @Override
    public MonthlySummaryDTO getMonthlySummary(Long memberId, Date startDate, Date endDate) {
        MonthlySummaryDTO monthlySummary = transactionMapper.getMonthlySummary(memberId, startDate, endDate);
        if (monthlySummary == null) {
            return new MonthlySummaryDTO();
        }
        return monthlySummary;
    }

    /**
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 일별 지출 내역 리스트
     */
    @Override
    public List<DailyExpenseDTO> getDailyExpense(Long memberId, Date startDate, Date endDate) {
        return transactionMapper.getDailyExpense(memberId, startDate, endDate);
    }

    /**
     * @param dailyList 일별 지출 내역
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 주간별 지출 내역
     */
    public List<WeeklyExpenseDTO> getWeeklyExpense(List<DailyExpenseDTO> dailyList, Date startDate, Date endDate) {
        List<WeeklyExpenseDTO> weeklyExpenseList = new ArrayList<>();

        if (dailyList == null || dailyList.isEmpty()) return weeklyExpenseList;

        // 1. Date -> LocalDate 변환
        LocalDate periodStart = toLocalDate(startDate);
        LocalDate periodEnd = toLocalDate(endDate);

        // 2. 일자별 지출 합산 맵 생성
        Map<LocalDate, Long> expenseByDate = dailyList.stream()
                .collect(Collectors.toMap(
                        dto -> toLocalDate(dto.getDate()),
                        DailyExpenseDTO::getTotalExpense,
                        Long::sum // 동일 날짜 합산
                ));

        // 3. 처리 시작일
        LocalDate cursor = periodStart;

        while (!cursor.isAfter(periodEnd)) {
            // 현재 달 기준
            LocalDate firstDayOfMonth = cursor.withDayOfMonth(1);
            LocalDate lastDayOfMonth = cursor.withDayOfMonth(cursor.lengthOfMonth());

            int weekNumber = 1;

            // 4. 첫 주: 1일부터 첫 토요일까지
            LocalDate weekStart = firstDayOfMonth;
            LocalDate weekEnd = weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            if (weekEnd.isAfter(lastDayOfMonth)) weekEnd = lastDayOfMonth;  // 이번 달의 마지막 날짜를 넘긴 경우, 마지막 날짜로 지정

            while (!weekStart.isAfter(lastDayOfMonth)) {
                long totalExpense = 0;
                LocalDate temp = weekStart;

                while (!temp.isAfter(weekEnd)) {
                    totalExpense += expenseByDate.getOrDefault(temp, 0L);
                    temp = temp.plusDays(1);
                }

                weeklyExpenseList.add(WeeklyExpenseDTO.builder()
                        .weekNumber(weekNumber)
                        .yearMonth(weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                        .startDate(toDate(weekStart))
                        .endDate(toDate(weekEnd))
                        .totalExpense(totalExpense)
                        .build());

                // 다음 주 준비
                weekStart = weekEnd.plusDays(1);
                weekEnd = weekStart.plusDays(6);
                if (weekEnd.isAfter(lastDayOfMonth)) weekEnd = lastDayOfMonth;

                weekNumber++;
            }

            // 다음 달로 이동
            cursor = lastDayOfMonth.plusDays(1);
        }

        return weeklyExpenseList;
    }

    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public List<CategoryExpenseDTO> getCategoryExpense(Long memberId, Date startDate, Date endDate) {
        return transactionMapper.getExpensesByCategory(memberId, startDate, endDate);
    }


    @Override
    public List<TransactionDetailDTO> getTransactionDetailsByAccountId(Long memberId, Long accountId, Date startDate, Date endDate) {
        // 1. 계좌 소유권 검증 및 실제 계좌번호 조회
        String resAccount = accountMapper.getResAccountById(accountId, memberId);

        if (resAccount == null) {
            throw new SecurityException("해당 계좌에 대한 권한이 없거나 존재하지 않는 계좌입니다.");
        }

        // 2. 실제 계좌번호로 거래내역 조회 (DTO 직접 반환)
        return transactionMapper.getTransactionDetailsByAccountId(memberId, resAccount, startDate, endDate);
    }

    @Override
    public List<TransactionDetailDTO> getTransactionDetailsByCategoryId(Long memberId, Long categoryId, Date startDate, Date endDate) {
        return transactionMapper.getTransactionDetailsByCategoryId(memberId, categoryId, startDate, endDate);
    }

}

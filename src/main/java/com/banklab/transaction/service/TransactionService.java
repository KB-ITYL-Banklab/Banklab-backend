package com.banklab.transaction.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.dto.SummaryDTO;
import com.banklab.transaction.dto.WeeklyExpenseDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;

    /**
     * 
     * @param startDate 시작일
     * @param endDate   종료일
     * @param account   계좌 번호
     * @return 월, 주간, 일간 내역 리스트
     */
    public SummaryDTO getSummary(Date startDate, Date endDate, String account) {
        MonthlySummaryDTO monthlySummary = getMonthlySummary(startDate, endDate, account);
        List<DailyExpenseDTO> dailyExpense = getDailyExpense(startDate, endDate, account);
        List<WeeklyExpenseDTO> weeklyExpense = getWeeklyExpense(dailyExpense, startDate, endDate);

        return SummaryDTO.builder()
                .monthlySummary(monthlySummary)
                .dailyExpense(dailyExpense)
                .weeklyExpense(weeklyExpense)
                .build();
    }

    /**
     * 
     * @param startDate 시작일
     * @param endDate   종료일
     * @param account   계좌번호
     * @return  해당 월 총수입 & 지출
     */
    public MonthlySummaryDTO getMonthlySummary(Date startDate, Date endDate, String account) {
        MonthlySummaryDTO monthlySummary = transactionMapper.getMonthlySummary(startDate, endDate, account);
        if (monthlySummary == null) {
            return new MonthlySummaryDTO();
        }
        return monthlySummary;
    }

    /**
     * 
     * @param startDate 시작일
     * @param endDate   종료일
     * @param account   계좌번호
     * @return  일별 지출 내역 리스트
     */
    public List<DailyExpenseDTO> getDailyExpense(Date startDate, Date endDate, String account) {
        return transactionMapper.getDailyExpense(startDate, endDate, account);
    }

    /**
     * 
     * @param dailyList 일별 지출 내역 
     * @param startDate  시작일
     * @param endDate 종료일
     * @return  주간별 지출 내역
     */
    public List<WeeklyExpenseDTO> getWeeklyExpense(List<DailyExpenseDTO> dailyList, Date startDate, Date endDate) {
        List<WeeklyExpenseDTO> weeklyExpenselist = new ArrayList<>();
        
        // 일별 지출 정보가 없는 경우 빈 list 반환
        if(dailyList == null || dailyList.isEmpty())  return weeklyExpenselist;

        //1. 기간의 시작, 마지막 일 구하기
        LocalDate periodStart = toLocalDate(startDate);
        LocalDate periodEnd = toLocalDate(endDate);


        Map<String, Integer> monthToWeekCounter = new HashMap<>();

        // 주차 기준 시작
        LocalDate weekStart = periodStart;
        LocalDate weekEnd = weekStart.plusDays(6);

        if (weekEnd.isAfter(periodEnd)) weekEnd = periodEnd;

        WeeklyExpenseDTO curWeek = null;
        long totalExpense = 0;
        for(DailyExpenseDTO daily : dailyList) {
            LocalDate localDate = toLocalDate(daily.getDate());

            // 월 정보 (예: "2025-07")
            String yearMonth =daily.getYearMonth();

            // 주차 초기화
            if (curWeek == null || localDate.isAfter(weekEnd)) {
                // 현재 주차 종료
                if (curWeek != null) {
                    curWeek.setTotalExpense(totalExpense);
                    weeklyExpenselist.add(curWeek);
                    totalExpense = 0;
                }

                // 새로운 주차 시작일 계산
                weekStart = localDate;
                // 현재 월 마지막 날짜
                LocalDate currentMonthLastDay = weekStart.withDayOfMonth(weekStart.lengthOfMonth());


                // 주차 끝 날짜 후보 계산
                weekEnd = weekStart.plusDays(6);
                if (weekEnd.isAfter(periodEnd)) {
                    weekEnd = periodEnd;
                }
                if (weekEnd.isAfter(currentMonthLastDay)) {
                    weekEnd = currentMonthLastDay;
                }

                // 주차 번호 계산
                int weekNum = monthToWeekCounter.getOrDefault(yearMonth, 0) + 1;
                monthToWeekCounter.put(yearMonth, weekNum);

                curWeek = WeeklyExpenseDTO.builder()
                        .weekNumber(weekNum)
                        .yearMonth(yearMonth)
                        .startDate(toDate(weekStart))
                        .endDate(toDate(weekEnd))
                        .build();
            }

            totalExpense += daily.getTotalExpense();
        }
        
        // 마지막 주차 추가
        if (curWeek != null) {
            curWeek.setTotalExpense(totalExpense);
            weeklyExpenselist.add(curWeek);
        }

        return  weeklyExpenselist;
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


    /**
     * 
     * @param num 주차
     * @param start 주차 시작 일
     * @param end   주차 마지막 일
     * @return 주간
     */
    private WeeklyExpenseDTO createNewWeek(int num, LocalDate start,LocalDate end){
        return  WeeklyExpenseDTO.builder()
                .weekNumber(num)
                .startDate(java.sql.Date.valueOf(start))
                .endDate(java.sql.Date.valueOf(end))
                .totalExpense(0L)
                .build();
    }

    public List<CategoryExpenseDTO> getCategoryExpense(Date startDate, Date endDate) {
        return transactionMapper.getExpensesByCategory(startDate, endDate);
    }


}

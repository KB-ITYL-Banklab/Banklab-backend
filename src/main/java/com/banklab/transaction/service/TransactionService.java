package com.banklab.transaction.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.dto.SummaryDTO;
import com.banklab.transaction.dto.WeeklyExpenseDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;

    /**
     * 
     * @param year 년도
     * @param month 월
     * @param account   계좌 번호
     * @return 월, 주간, 일간 내역 리스트
     */
    public SummaryDTO getSummary(int year, int month, String account) {
        MonthlySummaryDTO monthlySummary = getMonthlySummary(year, month, account);
        List<DailyExpenseDTO> dailyExpense = getDailyExpense(year, month, account);
        List<WeeklyExpenseDTO> weeklyExpense = getWeeklyExpense(dailyExpense, year, month);

        return SummaryDTO.builder()
                .monthlySummary(monthlySummary)
                .dailyExpense(dailyExpense)
                .weeklyExpense(weeklyExpense)
                .build();
    }

    /**
     * 
     * @param year 현재 년
     * @param month 현재 월
     * @param account   계좌번호
     * @return  해당 월 총수입 & 지출
     */
    public MonthlySummaryDTO getMonthlySummary(int year, int month, String account) {
        MonthlySummaryDTO monthlySummary = transactionMapper.getMonthlySummary(year, month, account);
        if (monthlySummary == null) {
            return new MonthlySummaryDTO();
        }
        return monthlySummary;
    }

    /**
     * 
     * @param year 현재 년
     * @param month 현재 월
     * @param account   계좌번호
     * @return  일별 지출 내역 리스트
     */
    public List<DailyExpenseDTO> getDailyExpense(int year, int month, String account) {
        return transactionMapper.getDailyExpense(year, month, account).stream().toList();
    }

    /**
     * 
     * @param dailyList 일별 지출 내역 
     * @param year  현재 년
     * @param month 현재 월
     * @return  주간별 지출 내역
     */
    public List<WeeklyExpenseDTO> getWeeklyExpense(List<DailyExpenseDTO> dailyList, int year, int month) {
        List<WeeklyExpenseDTO> weeklyExpenselist = new ArrayList<>();
        
        // 일별 지출 정보가 없는 경우 빈 list 반환
        if(dailyList == null || dailyList.isEmpty()) {
            return weeklyExpenselist;
        }

        //1. 월의 시작, 마지막 일 구하기
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        
        // 2. 주차: 1일 부터 한 주
        int weekNum = 1;
        LocalDate weekStart = monthStart;
        LocalDate weekEnd = weekStart.plusDays(6);
        if(weekEnd.isAfter(monthEnd)) {
            weekEnd = monthEnd;
        }

        WeeklyExpenseDTO curWeek = createNewWeek(weekNum, weekStart, weekEnd);

        long totalExpense = 0;
        for(DailyExpenseDTO daily : dailyList) {
            LocalDate localDate = daily.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if(localDate.isAfter(weekEnd)) {
                curWeek.setTotalExpense(totalExpense);

                weeklyExpenselist.add(curWeek);
                totalExpense=0;
                weekNum++;
                weekStart=weekEnd.plusDays(1);
                weekEnd=weekStart.plusDays(6);

                if(weekEnd.isAfter(monthEnd)) {
                    weekEnd=monthEnd;
                }

                curWeek =  createNewWeek(weekNum, weekStart, weekEnd);
            }
            totalExpense+=daily.getTotalExpense();
        }
        if(!weeklyExpenselist.contains(curWeek)) {
            curWeek.setTotalExpense(totalExpense);
            weeklyExpenselist.add(curWeek);
        }

        return  weeklyExpenselist;
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

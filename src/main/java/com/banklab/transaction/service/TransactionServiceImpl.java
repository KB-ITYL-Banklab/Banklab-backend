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

    /**
     * 전달받은 거래 내역 리스트를 DB에 저장합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 정보
     * @param transactions 저장할 거래 내역 리스트
     */
    @Override
    @Transactional
    public void saveTransactionList(Long memberId,AccountVO account, List<TransactionHistoryVO> transactions) {
        if(transactions.isEmpty())  return;
        for(TransactionHistoryVO t: transactions){
            transactionMapper.saveTransaction(t);
        }
    }

    /**
     * 특정 계좌의 마지막 거래 일자를 조회합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 번호
     * @return 마지막 거래 일자 (LocalDate)
     */
    @Override
    public LocalDate getLastTransactionDay(Long memberId, String account) {
        return transactionMapper.getLastTransactionDate(memberId, account);
    }

    /**
     * 거래 내역 리스트의 카테고리 정보를 일괄 업데이트합니다.
     * @param transactions 카테고리 정보가 포함된 거래 내역 리스트
     */
    @Transactional
    public void updateCategories(List<TransactionHistoryVO> transactions){
        transactionMapper.updateCategories(transactions);
    }

    /**
     * 특정 상호명을 가진 모든 거래 내역의 카테고리를 일괄 변경합니다.
     * @param categoryId 변경할 카테고리 ID
     * @param desc 대상 상호명
     * @param memberId 사용자 ID
     */
    @Override
    @Transactional
    public void updateCategoryByDesc(Long categoryId, String desc, Long memberId) {
        transactionMapper.updateCategoryByDesc(memberId, categoryId,  desc);
    }


    /**
     * 지정된 기간 동안의 소비/수입 내역을 요약하여 제공합니다.
     * 날짜가 지정되지 않은 경우, 현재 월의 1일부터 마지막 날까지를 기본값으로 사용합니다.
     * @param memberId 사용자 id
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 월별, 주간별, 일별, 카테고리별 소비 요약 정보를 담은 DTO
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
     * 지정된 기간 동안의 총 수입과 총 지출을 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 해당 기간의 총 수입과 지출 정보를 담은 DTO
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
     * 지정된 기간 동안의 일별 지출 내역 리스트를 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 일별 지출 내역 DTO 리스트
     */
    @Override
    public List<DailyExpenseDTO> getDailyExpense(Long memberId, Date startDate, Date endDate) {
        return transactionMapper.getDailyExpense(memberId, startDate, endDate);
    }

    /**
     * 일별 지출 내역 리스트를 기반으로 주간별 지출 내역을 계산합니다.
     * 각 월의 1일부터 첫 토요일까지를 1주차로 계산하고, 이후 7일 단위로 주차를 나눕니다.
     * @param dailyList 일별 지출 내역
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 주간별 지출 내역 DTO 리스트
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

    /**
     * java.util.Date 또는 java.sql.Date를 LocalDate로 변환합니다.
     * @param date 변환할 Date 객체
     * @return 변환된 LocalDate 객체
     */
    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDate를 java.util.Date로 변환합니다.
     * @param localDate 변환할 LocalDate 객체
     * @return 변환된 Date 객체
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 지정된 기간 동안의 카테고리별 지출 내역을 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 카테고리별 지출 DTO 리스트
     */
    public List<CategoryExpenseDTO> getCategoryExpense(Long memberId, Date startDate, Date endDate) {
        return transactionMapper.getExpensesByCategory(memberId, startDate, endDate);
    }


    /**
     * 특정 계좌(accountId)에 대한 거래 내역 상세 정보를 조회합니다.
     * 계좌 소유권을 검증하여, 요청한 사용자의 계좌가 맞는지 확인합니다.
     * @param memberId 사용자 ID
     * @param accountId 계좌의 고유 ID (PK)
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 거래 내역 상세 DTO 리스트
     * @throws SecurityException 계좌 소유권이 없거나 계좌가 존재하지 않을 경우
     */
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

    /**
     * 특정 카테고리에 해당하는 거래 내역 상세 정보를 조회합니다.
     * @param memberId 사용자 ID
     * @param categoryId 카테고리 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 거래 내역 상세 DTO 리스트
     */
    @Override
    public List<TransactionDetailDTO> getTransactionDetailsByCategoryId(Long memberId, Long categoryId, Date startDate, Date endDate) {
        return transactionMapper.getTransactionDetailsByCategoryId(memberId, categoryId, startDate, endDate);
    }

    /**
     * 특정 상호명을 가진 거래들의 발생 일자 리스트를 조회합니다.
     * (카테고리 수동 변경 후, 해당 거래일들의 집계 데이터를 업데이트하기 위해 사용)
     * @param memberId 사용자 ID
     * @param description 상호명
     * @return 날짜(Date) 객체 리스트
     */
    @Override
    public List<Date> getTransactionDates(Long memberId, String description) {
        return transactionMapper.getTransactionDates(memberId, description);
    }

}

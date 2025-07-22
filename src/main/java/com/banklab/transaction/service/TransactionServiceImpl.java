package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.perplexity.service.PerplexityService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.*;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactoinService {

    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final Map<String, Long> categoryMap;
    private final PerplexityService perplexityService;


    @Override
    public int saveTransaction(TransactionHistoryVO transaction) {
        return transactionMapper.saveTransaction(transaction);
    }

    @Override
    public int saveTransactionList(List<TransactionHistoryVO> transactionVOList) {
        return transactionMapper.saveTransactionList(transactionVOList);
    }

    /**
     * 
     * @param memberId 사용자 id
     * @param request  거래 내역 조회를 위한 요청 파라미터
     * @return  저장된 전체 거래 내역 개수
     */
    public int getTransactions(long memberId, TransactionRequestDto request) {
        int row = 0;
        // 1. 사용자의 전체 계좌 목록 가져오기
        List<AccountVO> userAccounts = accountMapper.selectAccountsByUserId(memberId);

        //2. 계좌별 거래 내역 조회
        for (AccountVO account : userAccounts) {
            TransactionDTO dto = TransactionDTO.builder()
                    .account(account.getResAccount())
                    .organization(account.getOrganization())
                    .connectedId(account.getConnectedId())
                    .orderBy(request.getOrderBy())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .build();
            List<TransactionHistoryVO> transactions;

            try {
                transactions = TransactionResponse.requestTransactions(dto);
                transactions = desTocategory(transactions);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 3. db에 거래 내역 저장
            row += saveTransactionList(transactions);
        }
        return row;
    }

    /**
     * api의 description (상호명)을 바탕으로 카테고리 추가
     * @param transactions: 거래 내역 리스트
     * @return 카테고리를 추가한 변환한 거래 내역 리스트
     */
    public List<TransactionHistoryVO> desTocategory(List<TransactionHistoryVO> transactions) {
        List<String> desc = transactions.stream()
                .map(TransactionHistoryVO::getDescription)
                .collect(Collectors.toList());

        List<String> categories = perplexityService.getCompletions(desc);
        while(categories.size()<desc.size()) {
            categories.add("기타");
        }

        for(int i = 0; i < transactions.size(); i++){
            String categoryName = categories.get(i).trim();
            Long categoryId = categoryMap.getOrDefault(categoryName, 8L); // Map에서 ID 조회
            transactions.get(i).setCategory_id(categoryId);
        }

        return transactions;
    }

    /**
     * @param memberId 사용자 id
     * @param startDate 시작일
     * @param endDate   종료일*
     * @return 월, 주간, 일간 내역 리스트
     */
    @Override
    public SummaryDTO getSummary(Long memberId, Date startDate, Date endDate) {

        List<AccountVO> accounts = accountMapper.selectAccountsByUserId(memberId);
        List<String> accountList = accounts.stream()
                .map(AccountVO::getResAccount)
                .collect(Collectors.toList());


        List<AccountSummaryDTO> accountSummaries = new ArrayList<>();

        for(String account : accountList){
            MonthlySummaryDTO monthlySummary = getMonthlySummary(startDate, endDate, account);
            List<DailyExpenseDTO> dailyExpense = getDailyExpense(startDate, endDate, account);
            List<WeeklyExpenseDTO> weeklyExpense = getWeeklyExpense(dailyExpense, startDate, endDate);
            List<CategoryExpenseDTO> categoryExpense = getCategoryExpense(startDate, endDate, account);

            AccountSummaryDTO summary = AccountSummaryDTO.builder()
                    .account(account)
                    .monthlySummary(monthlySummary)
                    .dailyExpense(dailyExpense)
                    .weeklyExpense(weeklyExpense)
                    .categoryExpense(categoryExpense)
                    .build();

            accountSummaries.add(summary);
        }

        return SummaryDTO.builder()
                .accountSummaries(accountSummaries)
                .build();
    }

    /**
     * @param startDate 시작일
     * @param endDate   종료일
     * @param account   계좌번호
     * @return 해당 월 총수입 & 지출
     */
    @Override
    public MonthlySummaryDTO getMonthlySummary(Date startDate, Date endDate, String account) {
        MonthlySummaryDTO monthlySummary = transactionMapper.getMonthlySummary(startDate, endDate, account);
        if (monthlySummary == null) {
            return new MonthlySummaryDTO();
        }
        return monthlySummary;
    }

    /**
     * @param startDate 시작일
     * @param endDate   종료일
     * @param account   계좌번호
     * @return 일별 지출 내역 리스트
     */
    @Override
    public List<DailyExpenseDTO> getDailyExpense(Date startDate, Date endDate, String account) {
        return transactionMapper.getDailyExpense(startDate, endDate, account);
    }

    /**
     * @param dailyList 일별 지출 내역
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 주간별 지출 내역
     */

    public List<WeeklyExpenseDTO> getWeeklyExpense(List<DailyExpenseDTO> dailyList, Date startDate, Date endDate) {
        List<WeeklyExpenseDTO> weeklyExpenselist = new ArrayList<>();

        // 일별 지출 정보가 없는 경우 빈 list 반환
        if (dailyList == null || dailyList.isEmpty()) return weeklyExpenselist;

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
        for (DailyExpenseDTO daily : dailyList) {
            LocalDate localDate = toLocalDate(daily.getDate());

            // 월 정보 (예: "2025-07")
            String yearMonth = daily.getYearMonth();

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

        return weeklyExpenselist;
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

    public List<CategoryExpenseDTO> getCategoryExpense(Date startDate, Date endDate, String account) {
        return transactionMapper.getExpensesByCategory(startDate, endDate, account);
    }


}

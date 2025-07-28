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
import com.banklab.transaction.summary.service.SummaryBatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final SummaryBatchService summaryBatchService;
    private final Map<String, Long> categoryMap;
    private final PerplexityService perplexityService;


    @Override
    public int saveTransaction(TransactionHistoryVO transaction) {
        return transactionMapper.saveTransaction(transaction);
    }

    @Override
    public int saveTransactionList(List<TransactionHistoryVO> transactionVOList) {
        if(transactionVOList.isEmpty()){return 0;}
        try {
            log.info("샘플 거래 내역: {}", new ObjectMapper().writeValueAsString(transactionVOList.get(0)));
        } catch (JsonProcessingException e) {
            log.error("Json Processing Exception", e);
        }
        return transactionMapper.saveTransactionList(transactionVOList);
    }

    @Override
    public LocalDate getLastTransactionDay(Long memberId) {
        return transactionMapper.getLastTransactionDate(memberId);
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

        //2. 계좌별 거래 내역 조회 api 호출
        for (AccountVO account : userAccounts) {
            TransactionDTO dto = makeTransactionDTO(account, request);
            List<TransactionHistoryVO> transactions;
            try {
                transactions = TransactionResponse.requestTransactions(memberId,dto);
                transactions = desTocategory(transactions);
            } catch (IOException | InterruptedException e) {
                log.error("거래 내역 불러오는 중 오류 발생");
                throw new RuntimeException(e);
            }

            // 3. db에 거래 내역 저장
            row += saveTransactionList(transactions);

            // 4. 집계 테이블에 집계 정보 저장
            summaryBatchService.initDailySummary(memberId);
        }
        return row;
    }

    /**
     *
     * @param account 계좌 정보
     * @param request 거래 내역 조회를 위한 요청 파라미터 (sDate, eDate, orderBy)
     * @return  거래 내역 조회를 위한 요청 DTO
     */
    public TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request){
        if(request == null){
            request = new TransactionRequestDto();
            LocalDate endDate   = LocalDate.now();
            LocalDate startDate = endDate.minusYears(5);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            request.setStartDate(startDate.format(formatter)); // "20190601" 형식
            request.setEndDate(endDate.format(formatter));     // 오늘 날짜 형식
            request.setOrderBy("0");
        }

        return TransactionDTO.builder()
                .account(account.getResAccount())
                .organization(account.getOrganization())
                .connectedId(account.getConnectedId())
                .orderBy(request.getOrderBy())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    /**
     * api의 description (상호명)을 바탕으로 카테고리 추가
     * @param transactions: 거래 내역 리스트
     * @return 카테고리를 추가한 변환한 거래 내역 리스트
     */
    public List<TransactionHistoryVO> desTocategory(List<TransactionHistoryVO> transactions) {

        log.info("카테고리 분류");
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
            String yearMonth = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

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

}

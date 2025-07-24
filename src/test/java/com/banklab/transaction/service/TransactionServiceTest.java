package com.banklab.transaction.service;

import com.banklab.codeapi.config.RestTemplateConfig;
import com.banklab.config.RootConfig;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.dto.response.WeeklyExpenseDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class})
@Transactional
@Log4j2
class TransactionServiceTest {

    @Autowired
    private TransactionServiceImpl transactionServiceImpl;

    String ExistentAccount = "110568497184";
    String nonExistentAccount = "0000000000";
    Long memberId = 1L;


    @Test
    @DisplayName("월간 요약 정보 조회 테스트 - DB에 데이터가 없는 경우")
    void getMonthlySummary_whenNoDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JULY, 1);
        Date startDate = cal.getTime();
        cal.set(2024, Calendar.JULY, 31);
        Date endDate = cal.getTime();

        // when
        MonthlySummaryDTO actualSummary = transactionServiceImpl.getMonthlySummary(startDate, endDate, nonExistentAccount);

        // then
        assertNotNull(actualSummary);
        assertEquals(0, actualSummary.getTotalIncome());
        assertEquals(0, actualSummary.getTotalExpense());
    }

    @Test
    @DisplayName("월간 요약 정보 조회 테스트 - DB 데이터 존재")
    void getMonthlySummary_whenDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JULY, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JULY, 31);
        Date endDate = cal.getTime();

        long expectedTotalIncome = 870666L;
        long expectedTotalOutcome = 716479L;

        //when
        MonthlySummaryDTO monthlySummary = transactionServiceImpl.getMonthlySummary(startDate, endDate, ExistentAccount);

        assertEquals(expectedTotalOutcome, monthlySummary.getTotalExpense());
        assertEquals(expectedTotalIncome, monthlySummary.getTotalIncome());
    }

    @Test
    @DisplayName("일 별 지출 정보 조회 테스트 - DB 데이터 존재")
    void getDailyExpense_whenDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JUNE, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JUNE, 30);
        Date endDate = cal.getTime();

        //when
        List<DailyExpenseDTO> dailyExpense = transactionServiceImpl.getDailyExpense(startDate, endDate, ExistentAccount);
        assertNotNull(dailyExpense);
    }

    @Test
    @DisplayName("일 별 지출 정보 조회 테스트 - DB 데이터 미존재")
    void getDailyExpense_whenNoDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JULY, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JULY, 31);
        Date endDate = cal.getTime();

        //when
        List<DailyExpenseDTO> dailyExpense = transactionServiceImpl.getDailyExpense(startDate, endDate, nonExistentAccount);

        assertEquals(dailyExpense.size(), 0);
    }


    @Test
    @DisplayName("주차별 지출 정보 조회 테스트 - DB 데이터 존재")
    void getWeeklyExpense_whenDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JUNE, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JUNE, 30);
        Date endDate = cal.getTime();

        //when
        List<DailyExpenseDTO> dailyExpense = transactionServiceImpl.getDailyExpense(startDate, endDate, ExistentAccount);
        List<WeeklyExpenseDTO> weeklyExpense= transactionServiceImpl.getWeeklyExpense(dailyExpense, startDate, endDate);

        for (WeeklyExpenseDTO week : weeklyExpense) {
            log.info("week: "+week.getWeekNumber()+" "+week.getStartDate()+" "+week.getEndDate()+" "+week.getTotalExpense());
        }
        assertNotNull(weeklyExpense);
    }

    @Test
    @DisplayName("주차별 지출 정보 조회 테스트 - DB 데이터 미존재")
    void getWeeklyExpense_whenNoDataInDB() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JULY, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JULY, 31);
        Date endDate = cal.getTime();

        //when
        List<DailyExpenseDTO> dailyExpense = transactionServiceImpl.getDailyExpense(startDate, endDate, nonExistentAccount);
        List<WeeklyExpenseDTO> weeklyExpense= transactionServiceImpl.getWeeklyExpense(dailyExpense, startDate, endDate);


        assertEquals(weeklyExpense.size(), 0);
    }

    @Test
    @DisplayName("월, 주간, 일별 통계 조회 - DB 데이터 존재")
    void getSummary_whenDataInDB() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JUNE, 1);
        Date startDate = cal.getTime();
        cal.set(2025, Calendar.JUNE, 30);
        Date endDate = cal.getTime();

        SummaryDTO summary = transactionServiceImpl.getSummary(memberId, startDate, endDate);
        assertNotNull(summary);
    }

    @Test
    @DisplayName("월, 주간, 일별 통계 조회 - DB 데이터 미존재")
    void getSummary_whenNoDataInDB() {
        Calendar cal = Calendar.getInstance();
        cal.set(2021, Calendar.JUNE, 1);
        Date startDate = cal.getTime();
        cal.set(2021, Calendar.JUNE, 30);
        Date endDate = cal.getTime();

        SummaryDTO summary = transactionServiceImpl.getSummary(memberId, startDate, endDate);
        assertEquals(summary.getAccountSummaries().get(0).getMonthlySummary().getTotalIncome(), 0);
        assertEquals(summary.getAccountSummaries().get(0).getMonthlySummary().getTotalExpense(), 0);
    }
}

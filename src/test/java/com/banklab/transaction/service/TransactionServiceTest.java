package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.AccountSummaryDTO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit5와 Mockito를 사용하여 TransactionServiceImpl의 단위 테스트를 수행하는 클래스입니다.
 * 각 메서드의 기능이 독립적으로 정확하게 동작하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@Log4j2
@DisplayName("TransactionService 단위 테스트")
class TransactionServiceTest {

    // @Mock: TransactionMapper의 모의 객체를 생성합니다. 실제 데이터베이스와 상호작용하지 않습니다.
    @Mock
    private TransactionMapper transactionMapper;

    // @InjectMocks: @Mock으로 생성된 모의 객체를 TransactionServiceImpl에 주입하여 테스트 대상을 생성합니다.
    @InjectMocks
    private TransactionServiceImpl transactionService;

    // 테스트에 사용될 공통 데이터입니다.
    private Long memberId;
    private AccountVO testAccount;
    private List<TransactionHistoryVO> testTransactions;

    /**
     * 각 테스트 실행 전에 필요한 공통 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        memberId = 1L;

        // 테스트용 계좌 객체를 생성합니다.
        testAccount = AccountVO.builder()
                .id(1L)
                .memberId(memberId)
                .resAccount("123-456-789")
                .build();

        // 테스트용 거래 내역 리스트를 생성합니다.
        testTransactions = Arrays.asList(
                TransactionHistoryVO.builder().id(1L).description("Transaction 1").resAccountOut(10000L).build(),
                TransactionHistoryVO.builder().id(2L).description("Transaction 2").resAccountOut(20000L).build()
        );
    }

    @Test
    @DisplayName("거래 내역 저장 - 성공")
    void saveTransactionList_Success() {
        // when: 거래 내역 저장 메서드를 호출합니다.
        transactionService.saveTransactionList(memberId, testAccount, testTransactions);

        // then: transactionMapper의 saveTransactionList가 1번 호출되었는지 검증합니다.
        verify(transactionMapper, times(1)).saveTransactionList(testTransactions);
    }

    @Test
    @DisplayName("거래 내역 저장 - 빈 리스트")
    void saveTransactionList_EmptyList() {
        // when: 빈 거래 내역 리스트로 저장 메서드를 호출합니다.
        transactionService.saveTransactionList(memberId, testAccount, Collections.emptyList());

        // then: transactionMapper의 saveTransactionList가 호출되지 않았는지 검증합니다.
        verify(transactionMapper, never()).saveTransactionList(any());
    }

    @Test
    @DisplayName("마지막 거래일 조회 - 성공")
    void getLastTransactionDay_Success() {
        // given: 모의 객체가 반환할 마지막 거래일을 설정합니다.
        LocalDate expectedDate = LocalDate.of(2024, 8, 4);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(expectedDate);

        // when: 마지막 거래일 조회 메서드를 호출합니다.
        LocalDate actualDate = transactionService.getLastTransactionDay(memberId, testAccount.getResAccount());

        // then: 반환된 날짜가 예상과 일치하는지 검증합니다.
        assertEquals(expectedDate, actualDate);
        verify(transactionMapper, times(1)).getLastTransactionDate(memberId, testAccount.getResAccount());
    }

    @Test
    @DisplayName("카테고리 업데이트 - 성공")
    void updateCategories_Success() {
        // when: 카테고리 업데이트 메서드를 호출합니다.
        transactionService.updateCategories(testTransactions);

        // then: transactionMapper의 updateCategories가 1번 호출되었는지 검증합니다.
        verify(transactionMapper, times(1)).updateCategories(testTransactions);
    }

    @Test
    @DisplayName("월별 요약 조회 - 데이터 있음")
    void getMonthlySummary_WithData() {
        // given: 모의 객체가 반환할 월별 요약 데이터를 설정합니다.
        Date startDate = Date.valueOf("2024-08-01");
        Date endDate = Date.valueOf("2024-08-31");
        MonthlySummaryDTO expectedSummary = new MonthlySummaryDTO();
        expectedSummary.setTotalIncome(500000L);
        expectedSummary.setTotalExpense(300000L);
        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate)).thenReturn(expectedSummary);

        // when: 월별 요약 조회 메서드를 호출합니다.
        MonthlySummaryDTO actualSummary = transactionService.getMonthlySummary(memberId, startDate, endDate);

        // then: 반환된 요약 데이터가 예상과 일치하는지 검증합니다.
        assertEquals(expectedSummary, actualSummary);
    }

    @Test
    @DisplayName("월별 요약 조회 - 데이터 없음 (null 반환)")
    void getMonthlySummary_NullData() {
        // given: 모의 객체가 null을 반환하도록 설정합니다.
        Date startDate = Date.valueOf("2024-08-01");
        Date endDate = Date.valueOf("2024-08-31");
        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate)).thenReturn(null);

        // when: 월별 요약 조회 메서드를 호출합니다.
        MonthlySummaryDTO actualSummary = transactionService.getMonthlySummary(memberId, startDate, endDate);

        // then: null이 반환될 경우, 기본값이 설정된 새로운 객체가 반환되는지 검증합니다.
        assertNotNull(actualSummary);
        assertEquals(0L, actualSummary.getTotalIncome());
        assertEquals(0L, actualSummary.getTotalExpense());
    }

    @Test
    @DisplayName("일별 지출 조회 - 성공")
    void getDailyExpense_Success() {
        // given: 모의 객체가 반환할 일별 지출 데이터를 설정합니다.
        Date startDate = Date.valueOf("2024-08-01");
        Date endDate = Date.valueOf("2024-08-31");
        List<DailyExpenseDTO> expectedExpenses = Arrays.asList(new DailyExpenseDTO(), new DailyExpenseDTO());
        when(transactionMapper.getDailyExpense(memberId, startDate, endDate)).thenReturn(expectedExpenses);

        // when: 일별 지출 조회 메서드를 호출합니다.
        List<DailyExpenseDTO> actualExpenses = transactionService.getDailyExpense(memberId, startDate, endDate);

        // then: 반환된 지출 데이터가 예상과 일치하는지 검증합니다.
        assertEquals(expectedExpenses, actualExpenses);
    }

    @Test
    @DisplayName("카테고리별 지출 조회 - 성공")
    void getCategoryExpense_Success() {
        // given: 모의 객체가 반환할 카테고리별 지출 데이터를 설정합니다.
        Date startDate = Date.valueOf("2024-08-01");
        Date endDate = Date.valueOf("2024-08-31");
        List<CategoryExpenseDTO> expectedExpenses = Arrays.asList(new CategoryExpenseDTO(), new CategoryExpenseDTO());
        when(transactionMapper.getExpensesByCategory(memberId, startDate, endDate)).thenReturn(expectedExpenses);

        // when: 카테고리별 지출 조회 메서드를 호출합니다.
        List<CategoryExpenseDTO> actualExpenses = transactionService.getCategoryExpense(memberId, startDate, endDate);

        // then: 반환된 지출 데이터가 예상과 일치하는지 검증합니다.
        assertEquals(expectedExpenses, actualExpenses);
    }

    @Test
    @DisplayName("전체 요약 조회 - 성공")
    void getSummary_Success() {
        // given: 날짜 범위를 설정하고, 각 요약 메서드가 반환할 모의 데이터를 준비합니다.
        Date startDate = Date.valueOf("2024-08-01");
        Date endDate = Date.valueOf("2024-08-31");

        MonthlySummaryDTO monthlySummary = new MonthlySummaryDTO();
        monthlySummary.setTotalExpense(15000L);
        monthlySummary.setTotalIncome(100000L);

        // DailyExpenseDTO 생성 시, date 필드를 명시적으로 초기화하여 null 오류를 방지합니다.
        DailyExpenseDTO dailyExpense = new DailyExpenseDTO();
        dailyExpense.setDate(startDate);
        dailyExpense.setTotalExpense(5000L);
        List<DailyExpenseDTO> dailyExpenses = Collections.singletonList(dailyExpense);

        List<CategoryExpenseDTO> categoryExpenses = Collections.singletonList(new CategoryExpenseDTO());

        // transactionMapper의 각 메서드가 호출될 때, 위에서 생성한 모의 데이터를 반환하도록 설정합니다.
        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate)).thenReturn(monthlySummary);
        when(transactionMapper.getDailyExpense(memberId, startDate, endDate)).thenReturn(dailyExpenses);
        when(transactionMapper.getExpensesByCategory(memberId, startDate, endDate)).thenReturn(categoryExpenses);

        // when: 전체 요약 조회 메서드를 호출합니다.
        SummaryDTO summary = transactionService.getSummary(memberId, startDate, endDate);

        // then: 반환된 SummaryDTO와 그 안의 AccountSummaryDTO가 null이 아닌지 확인합니다.
        assertNotNull(summary);
        assertNotNull(summary.getAccountSummaries());
        assertFalse(summary.getAccountSummaries().isEmpty());

        // then: 반환된 요약 정보가 모의 데이터와 일치하는지 검증합니다.
        AccountSummaryDTO accountSummary = summary.getAccountSummaries().get(0);
        assertEquals(monthlySummary, accountSummary.getMonthlySummary());
        assertEquals(dailyExpenses, accountSummary.getDailyExpense());
        assertEquals(categoryExpenses, accountSummary.getCategoryExpense());

        // then: 각 매퍼 메서드가 정확히 1번씩 호출되었는지 검증합니다.
        verify(transactionMapper, times(1)).getMonthlySummary(memberId, startDate, endDate);
        verify(transactionMapper, times(1)).getDailyExpense(memberId, startDate, endDate);
        verify(transactionMapper, times(1)).getExpensesByCategory(memberId, startDate, endDate);
    }

    @Test
    @DisplayName("전체 요약 조회 - 날짜가 null일 경우 기본값 사용")
    void getSummary_NullDates() {
        // given: 현재 월의 시작일과 종료일을 계산합니다.
        LocalDate now = LocalDate.now();
        Date expectedStartDate = Date.valueOf(now.withDayOfMonth(1));
        Date expectedEndDate = Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));

        // given: 날짜가 null일 때, 기본값으로 호출될 것을 예상하고 모의 객체를 설정합니다.
        when(transactionMapper.getMonthlySummary(eq(memberId), any(Date.class), any(Date.class))).thenReturn(new MonthlySummaryDTO());
        when(transactionMapper.getDailyExpense(eq(memberId), any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        when(transactionMapper.getExpensesByCategory(eq(memberId), any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());

        // when: 날짜를 null로 하여 전체 요약 조회를 호출합니다.
        transactionService.getSummary(memberId, null, null);

        // then: 매퍼 메서드들이 기본값으로 설정된 날짜(any(Date.class))로 1번씩 호출되었는지 검증합니다.
        verify(transactionMapper, times(1)).getMonthlySummary(eq(memberId), any(Date.class), any(Date.class));
        verify(transactionMapper, times(1)).getDailyExpense(eq(memberId), any(Date.class), any(Date.class));
        verify(transactionMapper, times(1)).getExpensesByCategory(eq(memberId), any(Date.class), any(Date.class));
    }
}
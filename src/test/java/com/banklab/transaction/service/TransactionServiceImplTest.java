package com.banklab.transaction.service;

// 테스트 대상이 되는 TransactionService와 관련 도메인 객체들을 import
import com.banklab.account.domain.AccountVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.category.service.CategoryService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;

// JUnit5 테스트 프레임워크 관련 어노테이션들을 import
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito 라이브러리로 모의 객체(Mock) 생성 및 검증을 위한 import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Java 표준 라이브러리 - Date, 컬렉션 등
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// 테스트 검증을 위한 assertion과 mockito 검증 메서드들
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit5와 Mockito를 사용하여 TransactionServiceImpl을 테스트하는 클래스
 * MockitoExtension을 통해 Mock 객체 자동 생성 및 주입 기능 활성화
 */
@ExtendWith(MockitoExtension.class)
@Log4j2
@DisplayName("TransactionService 테스트") // 테스트 실행 시 표시될 이름
class TransactionServiceImplTest {

    // @Mock: Mockito가 자동으로 모의 객체를 생성해주는 어노테이션
    @Mock
    private TransactionMapper transactionMapper; // 데이터베이스 접근을 위한 MyBatis 매퍼 모의 객체

    @Mock
    private SummaryBatchService summaryBatchService; // 집계 배치 서비스 모의 객체

    @Mock
    private CategoryService categoryService; // 카테고리 관련 서비스 모의 객체

    // @InjectMocks: 위에서 생성한 Mock 객체들을 자동으로 주입받는 실제 테스트 대상 객체
    @InjectMocks
    private TransactionServiceImpl transactionService; // 실제 테스트할 서비스 객체

    // 테스트에서 공통으로 사용할 테스트 데이터 변수들
    private AccountVO testAccount; // 테스트용 계좌 정보
    private List<TransactionHistoryVO> testTransactions; // 테스트용 거래 내역 리스트

    /**
     * 각 테스트 메서드 실행 전에 공통 테스트 데이터를 초기화하는 메서드
     * @BeforeEach로 모든 테스트 메서드 실행 전마다 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 계좌 객체 생성 - Builder 패턴 사용
        testAccount = AccountVO.builder()
                .id(1L) // 계좌 고유 ID
                .memberId(1L) // 계좌 소유자 회원 ID
                .resAccount("123456789") // 계좌번호
                .resAccountName("테스트계좌") // 계좌명
                .resAccountDisplay("123-456-789") // 화면 표시용 계좌번호
                .resAccountBalance("1000000") // 계좌 잔액
                .organization("0088") // 금융기관 코드
                .connectedId("test_connected_id") // CODEF 연동 ID
                .build();

        // 테스트용 거래 내역 리스트 생성 - 2개의 거래 내역을 포함
        testTransactions = Arrays.asList(
                // 첫 번째 거래: 스타벅스에서 5000원 출금
                TransactionHistoryVO.builder()
                        .id(1L) // 거래 고유 ID
                        .memberId(1L) // 거래한 회원 ID
                        .accountId(1L) // 거래가 발생한 계좌 ID
                        .resAccount("123456789") // 거래 발생 계좌번호
                        .transactionDate("20240115") // 거래 발생일 (yyyyMMdd 형식)
                        .transactionTime("143000") // 거래 발생시간 (HHmmss 형식)
                        .resAccountOut(5000L) // 출금액 (양수로 표현)
                        .resAfterTranBalance(95000L) // 거래 후 잔액
                        .description("스타벅스 강남점") // 거래 상대방(상호명)
                        .category_id(1L) // 분류된 카테고리 ID
                        .build(),
                // 두 번째 거래: 올리브영에서 15000원 출금
                TransactionHistoryVO.builder()
                        .id(2L)
                        .memberId(1L)
                        .accountId(1L)
                        .resAccount("123456789")
                        .transactionDate("20240116")
                        .transactionTime("160000")
                        .resAccountOut(15000L)
                        .resAfterTranBalance(80000L)
                        .description("올리브영 홍대점")
                        .category_id(2L)
                        .build()
        );
    }

    /**
     * 거래 내역 저장 기능의 정상적인 동작을 테스트
     */
    @Test
    @DisplayName("거래 내역 저장 - 정상 케이스")
    void saveTransactionList_Success() {
        // given: 테스트 조건 설정
        Long memberId = 1L; // 테스트할 회원 ID

        // when: 실제 테스트할 메서드 호출
        transactionService.saveTransactionList(memberId, testAccount, testTransactions);

        // then: 결과 검증 - transactionMapper.saveTransactionList가 정확히 1번 호출되었는지 확인
        verify(transactionMapper, times(1)).saveTransactionList(testTransactions);
    }

    /**
     * 빈 거래 내역 리스트가 전달되었을 때의 동작을 테스트
     * 실제 저장 작업이 수행되지 않아야 함
     */
    @Test
    @DisplayName("거래 내역 저장 - 빈 리스트인 경우")
    void saveTransactionList_EmptyList() {
        // given: 빈 거래 내역 리스트 생성
        Long memberId = 1L;
        List<TransactionHistoryVO> emptyTransactions = Collections.emptyList(); // 빈 리스트

        // when: 빈 리스트로 저장 메서드 호출
        transactionService.saveTransactionList(memberId, testAccount, emptyTransactions);

        // then: 실제 저장 메서드가 호출되지 않았는지 확인
        verify(transactionMapper, never()).saveTransactionList(any()); // never(): 한 번도 호출되지 않음을 검증
    }

    /**
     * 특정 계좌의 마지막 거래일을 조회하는 기능 테스트
     */
    @Test
    @DisplayName("마지막 거래일 조회")
    void getLastTransactionDay_Success() {
        // given: 예상 반환값 설정
        Long memberId = 1L;
        String account = "123456789";
        LocalDate expectedDate = LocalDate.of(2024, 1, 16); // 2024년 1월 15일

        // when: 실제 테스트할 메서드 호출
        transactionService.saveTransactionList(memberId, testAccount, testTransactions);

        // Mock 객체의 동작 정의: 해당 매개변수로 호출 시 expectedDate 반환
        when(transactionMapper.getLastTransactionDate(memberId, account)).thenReturn(expectedDate);

        // when: 실제 메서드 호출
        LocalDate result = transactionService.getLastTransactionDay(memberId, account);

        // then: 반환값이 예상값과 동일한지 확인
        assertEquals(expectedDate, result); // 반환값 검증
        log.info("excepted: {}, result:{}", expectedDate, result);
        verify(transactionMapper, times(1)).getLastTransactionDate(memberId, account); // 호출 횟수 검증
    }

    /**
     * 거래 내역의 카테고리 정보를 업데이트하는 기능 테스트
     */
    @Test
    @DisplayName("카테고리 업데이트")
    void updateCategories_Success() {
        // when: 카테고리 업데이트 메서드 호출
        transactionService.updateCategories(testTransactions);

        // then: mapper의 updateCategories가 정확한 매개변수로 1번 호출되었는지 검증
        verify(transactionMapper, times(1)).updateCategories(testTransactions);
    }

    /**
     * 월별 거래 요약 정보 조회 기능의 정상 동작 테스트
     */
    @Test
    @DisplayName("월별 요약 조회 - 정상 케이스")
    void getMonthlySummary_Success() {
        // given: 테스트 매개변수와 예상 반환값 설정
        Long memberId = 1L;
        Date startDate = Date.valueOf("2024-01-01"); // SQL Date 타입으로 시작일 설정
        Date endDate = Date.valueOf("2024-01-31"); // SQL Date 타입으로 종료일 설정
        
        // 예상 월별 요약 데이터 생성 (Setter 방식으로 생성 - Builder 패턴 미지원)
        MonthlySummaryDTO expectedSummary = new MonthlySummaryDTO();
        expectedSummary.setTotalIncome(1000000L); // 총 수입 100만원
        expectedSummary.setTotalExpense(500000L); // 총 지출 50만원

        // Mock 객체의 동작 정의: 매개변수 호출 시 예상 요약 데이터 반환
        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate))
                .thenReturn(expectedSummary);

        // when: 실제 메서드 호출
        MonthlySummaryDTO result = transactionService.getMonthlySummary(memberId, startDate, endDate);

        // then: 반환값이 예상값과 동일한지 검증
        assertEquals(expectedSummary, result);
        verify(transactionMapper, times(1)).getMonthlySummary(memberId, startDate, endDate);
    }

    /**
     * 월별 요약 조회 시 데이터가 없어 null이 반환되는 경우의 처리 테스트
     */
    @Test
    @DisplayName("월별 요약 조회 - null인 경우")
    void getMonthlySummary_Null() {
        // given: mapper에서 null 반환하도록 설정
        Long memberId = 1L;
        Date startDate = Date.valueOf("2024-01-01");
        Date endDate = Date.valueOf("2024-01-31");

        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate))
                .thenReturn(null); // null 반환 설정

        // when: 메서드 호출
        MonthlySummaryDTO result = transactionService.getMonthlySummary(memberId, startDate, endDate);

        // then: null이 아닌 새로운 객체가 반환되는지 확인 (기본값 처리)
        assertNotNull(result); // 결과가 null이 아님을 확인
        verify(transactionMapper, times(1)).getMonthlySummary(memberId, startDate, endDate);
    }

    /**
     * 일별 지출 내역 조회 기능 테스트
     */
    @Test
    @DisplayName("일별 지출 조회")
    void getDailyExpense_Success() {
        // given: 테스트 매개변수와 예상 반환값 설정
        Long memberId = 1L;
        Date startDate = Date.valueOf("2024-01-01");
        Date endDate = Date.valueOf("2024-01-31");
        
        // 예상 일별 지출 데이터 생성 (Setter 방식)
        DailyExpenseDTO dailyExpense = new DailyExpenseDTO();
        dailyExpense.setDate(startDate); // 지출 발생일
        dailyExpense.setTotalExpense(50000L); // 해당일 총 지출액
        List<DailyExpenseDTO> expectedExpenses = Arrays.asList(dailyExpense);

        // Mock 동작 정의
        when(transactionMapper.getDailyExpense(memberId, startDate, endDate))
                .thenReturn(expectedExpenses);

        // when: 메서드 호출
        List<DailyExpenseDTO> result = transactionService.getDailyExpense(memberId, startDate, endDate);

        // then: 반환값 검증
        assertEquals(expectedExpenses, result); // 리스트 내용이 동일한지 확인
        verify(transactionMapper, times(1)).getDailyExpense(memberId, startDate, endDate);
    }

    /**
     * 카테고리별 지출 내역 조회 기능 테스트
     */
    @Test
    @DisplayName("카테고리별 지출 조회")
    void getCategoryExpense_Success() {
        // given: 테스트 데이터 준비
        Long memberId = 1L;
        Date startDate = Date.valueOf("2024-01-01");
        Date endDate = Date.valueOf("2024-01-31");
        
        // 예상 카테고리별 지출 데이터 생성 (Builder 패턴 사용 가능)
        List<CategoryExpenseDTO> expectedExpenses = Arrays.asList(
                CategoryExpenseDTO.builder()
                        .categoryId(1L) // 카테고리 ID
                        .categoryName("카페") // 카테고리명
                        .totalExpense(100000L) // 해당 카테고리 총 지출액
                        .build()
        );

        // Mock 동작 정의
        when(transactionMapper.getExpensesByCategory(memberId, startDate, endDate))
                .thenReturn(expectedExpenses);

        // when: 메서드 호출
        List<CategoryExpenseDTO> result = transactionService.getCategoryExpense(memberId, startDate, endDate);

        // then: 결과 검증
        assertEquals(expectedExpenses, result);
        verify(transactionMapper, times(1)).getExpensesByCategory(memberId, startDate, endDate);
    }

    /**
     * 전체 요약 정보(월별+일별+카테고리별)를 통합 조회하는 기능의 정상 동작 테스트
     */
    @Test
    @DisplayName("전체 요약 조회 - 정상 케이스")
    void getSummary_Success() {
        // given: 복합적인 테스트 데이터 준비
        Long memberId = 1L;
        Date startDate = Date.valueOf("2024-01-01");
        Date endDate = Date.valueOf("2024-01-31");

        // 월별 요약 데이터 준비
        MonthlySummaryDTO monthlySummary = new MonthlySummaryDTO();
        monthlySummary.setTotalIncome(1000000L);
        monthlySummary.setTotalExpense(500000L);

        // 일별 지출 데이터 준비
        DailyExpenseDTO dailyExpense = new DailyExpenseDTO();
        dailyExpense.setDate(startDate);
        dailyExpense.setTotalExpense(50000L);
        List<DailyExpenseDTO> dailyExpenses = Arrays.asList(dailyExpense);

        // 카테고리별 지출 데이터 준비
        List<CategoryExpenseDTO> categoryExpenses = Arrays.asList(
                CategoryExpenseDTO.builder()
                        .categoryId(1L)
                        .categoryName("카페")
                        .totalExpense(100000L)
                        .build()
        );

        // 각각의 Mock 동작 정의 - getSummary 메서드 내부에서 호출되는 모든 메서드들
        when(transactionMapper.getMonthlySummary(memberId, startDate, endDate))
                .thenReturn(monthlySummary);
        when(transactionMapper.getDailyExpense(memberId, startDate, endDate))
                .thenReturn(dailyExpenses);
        when(transactionMapper.getExpensesByCategory(memberId, startDate, endDate))
                .thenReturn(categoryExpenses);

        // when: 통합 요약 메서드 호출
        SummaryDTO result = transactionService.getSummary(memberId, startDate, endDate);

        // then: 복합 결과 검증
        assertNotNull(result); // 결과 객체가 null이 아님
        assertNotNull(result.getAccountSummaries()); // 계좌 요약 리스트가 null이 아님
        assertEquals(1, result.getAccountSummaries().size()); // 계좌 요약이 1개 포함

        // 각 하위 메서드가 정확히 1번씩 호출되었는지 검증
        verify(transactionMapper, times(1)).getMonthlySummary(memberId, startDate, endDate);
        verify(transactionMapper, times(1)).getDailyExpense(memberId, startDate, endDate);
        verify(transactionMapper, times(1)).getExpensesByCategory(memberId, startDate, endDate);
    }

    /**
     * 날짜 매개변수가 null로 전달되었을 때 기본값으로 처리되는지 테스트
     */
    @Test
    @DisplayName("전체 요약 조회 - null 날짜인 경우 기본값 사용")
    void getSummary_NullDates() {
        // given: null 날짜와 기본 반환값 설정
        Long memberId = 1L;
        // null 날짜 전달 시 내부적으로 현재 월의 시작일~끝일로 기본값 설정됨을 가정
        LocalDate now = LocalDate.now();
        Date expectedStartDate = Date.valueOf(now.withDayOfMonth(1)); // 현재 월 1일
        Date expectedEndDate = Date.valueOf(now.withDayOfMonth(now.lengthOfMonth())); // 현재 월 마지막일

        // Mock 동작 정의 - any(Date.class)로 어떤 Date 객체든 매칭
        when(transactionMapper.getMonthlySummary(eq(memberId), any(Date.class), any(Date.class)))
                .thenReturn(new MonthlySummaryDTO());
        when(transactionMapper.getDailyExpense(eq(memberId), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList()); // 빈 리스트 반환
        when(transactionMapper.getExpensesByCategory(eq(memberId), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        // when: null 날짜로 메서드 호출
        SummaryDTO result = transactionService.getSummary(memberId, null, null);

        // then: 기본값 처리 결과 검증
        assertNotNull(result); // 결과 객체 존재 확인
        // eq()와 any()를 사용하여 정확한 매개변수 타입으로 호출되었는지 확인
        verify(transactionMapper, times(1)).getMonthlySummary(eq(memberId), any(Date.class), any(Date.class));
        verify(transactionMapper, times(1)).getDailyExpense(eq(memberId), any(Date.class), any(Date.class));
        verify(transactionMapper, times(1)).getExpensesByCategory(eq(memberId), any(Date.class), any(Date.class));
    }
}
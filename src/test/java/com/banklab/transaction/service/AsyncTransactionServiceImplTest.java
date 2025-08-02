package com.banklab.transaction.service;

// 테스트 대상인 AsyncTransactionService와 관련 도메인 객체들을 import
import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.service.CategoryService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;

// JUnit5 테스트 프레임워크 관련 어노테이션들을 import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito 라이브러리로 모의 객체(Mock) 생성 및 검증을 위한 import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

// Java 표준 라이브러리 - 예외 처리, 날짜, 컬렉션, 비동기 처리 등
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// 테스트 검증을 위한 assertion과 mockito 검증 메서드들
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit5와 Mockito를 사용하여 AsyncTransactionServiceImpl을 테스트하는 클래스
 * 비동기 거래 내역 처리 로직과 외부 API 호출을 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncTransactionService 테스트") // 테스트 실행 시 표시될 이름
class AsyncTransactionServiceImplTest {

    // @Mock: Mockito가 자동으로 모의 객체를 생성해주는 어노테이션들
    @Mock
    private TransactionMapper transactionMapper; // 거래내역 데이터베이스 접근을 위한 MyBatis 매퍼 모의 객체

    @Mock
    private AccountMapper accountMapper; // 계좌 정보 데이터베이스 접근을 위한 MyBatis 매퍼 모의 객체

    @Mock
    private TransactionService transactionService; // 동기 거래 서비스 모의 객체

    @Mock
    private CategoryService categoryService; // 카테고리 분류 서비스 모의 객체

    @Mock
    private SummaryBatchService summaryBatchService; // 집계 배치 서비스 모의 객체

    // @InjectMocks: 위에서 생성한 Mock 객체들을 자동으로 주입받는 실제 테스트 대상 객체
    @InjectMocks
    private AsyncTransactionServiceImpl asyncTransactionService; // 실제 테스트할 비동기 거래 서비스 객체

    // 테스트에서 공통으로 사용할 테스트 데이터 변수들
    private AccountVO testAccount; // 테스트용 계좌 정보
    private List<AccountVO> testAccounts; // 테스트용 계좌 리스트
    private List<TransactionHistoryVO> testTransactions; // 테스트용 거래 내역 리스트
    private TransactionRequestDto testRequest; // 테스트용 거래 내역 요청 DTO

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
                .organization("KB") // 금융기관 코드
                .connectedId("test_connected_id") // CODEF 연동 ID
                .build();

        // 테스트용 계좌 리스트 생성 - 단일 계좌 포함
        testAccounts = Arrays.asList(testAccount);

        // 테스트용 거래 내역 리스트 생성 - 2개의 거래 내역 포함
        testTransactions = Arrays.asList(
                // 첫 번째 거래: 스타벅스에서 5000원 출금
                TransactionHistoryVO.builder()
                        .id(1L) // 거래 고유 ID
                        .memberId(1L) // 거래한 회원 ID
                        .accountId(1L) // 거래가 발생한 계좌 ID
                        .resAccount("123456789") // 거래 발생 계좌번호
                        .transactionDate("20240115") // 거래 발생일 (yyyyMMdd 형식)
                        .transactionTime("143000") // 거래 발생시간 (HHmmss 형식)
                        .resAccountOut(5000L) // 출금액
                        .resAfterTranBalance(95000L) // 거래 후 잔액
                        .description("스타벅스 강남점") // 거래 상대방(상호명)
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
                        .build()
        );

        // 테스트용 거래 내역 요청 DTO 생성 - Builder 패턴 사용
        testRequest = TransactionRequestDto.builder()
                .resAccount("123456789") // 조회할 계좌번호
                .startDate("20240101") // 조회 시작일 (yyyyMMdd 형식)
                .endDate("20240131") // 조회 종료일 (yyyyMMdd 형식)
                .orderBy("0") // 정렬 순서 (0: 최신순, 1: 과거순)
                .build();
    }

    /**
     * 특정 계좌를 지정하여 거래 내역을 조회하는 기능의 정상 동작 테스트
     * 전체 비동기 처리 플로우 (API 호출 → DB 저장 → 카테고리 분류 → 집계 업데이트)를 검증
     */
    @Test
    @DisplayName("거래 내역 조회 - 특정 계좌 지정")
    void getTransactions_SpecificAccount() throws IOException, InterruptedException {
        // given: 테스트 조건 설정
        Long memberId = 1L;
        // 특정 계좌번호로 계좌 조회 시 테스트 계좌 반환하도록 설정
        when(accountMapper.getAccountByAccountNumber(testRequest.getResAccount()))
                .thenReturn(testAccount);
        // 해당 계좌의 마지막 거래일이 없다고 설정 (전체 거래 내역 조회)
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);
        // 카테고리 분류 서비스가 완료된 CompletableFuture를 반환하도록 설정
        when(categoryService.categorizeTransactions(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Static 메서드 모킹 - TransactionResponse.requestTransactions() 메서드를 모킹
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            // CODEF API 호출 시 테스트 거래 내역 리스트 반환하도록 설정
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(testTransactions);

            // when: 실제 테스트할 메서드 호출 - 특정 계좌 지정
            asyncTransactionService.getTransactions(memberId, testRequest);

            // then: 결과 검증
            // 특정 계좌번호로 계좌 조회가 1번 호출되었는지 확인
            verify(accountMapper, times(1)).getAccountByAccountNumber(testRequest.getResAccount());
            // 전체 계좌 조회는 호출되지 않았는지 확인 (특정 계좌 지정이므로)
            verify(accountMapper, never()).selectAccountsByUserId(memberId);
            // 거래 내역 저장이 올바른 매개변수로 1번 호출되었는지 확인
            verify(transactionService, times(1)).saveTransactionList(eq(memberId), eq(testAccount), eq(testTransactions));
            // 카테고리 분류가 올바른 거래 내역으로 1번 호출되었는지 확인
            verify(categoryService, times(1)).categorizeTransactions(testTransactions);
            // 집계 업데이트가 올바른 매개변수로 1번 호출되었는지 확인
            verify(summaryBatchService, times(1)).initDailySummary(memberId, testAccount);
        }
    }

    /**
     * 요청 매개변수가 null인 경우 전체 계좌의 거래 내역을 조회하는 기능 테스트
     * Batch 처리나 전체 계좌 업데이트 시나리오를 검증
     */
    @Test
    @DisplayName("거래 내역 조회 - 전체 계좌 (request null)")
    void getTransactions_AllAccounts_NullRequest() throws IOException, InterruptedException {
        // given: null 요청으로 전체 계좌 조회 시나리오 설정
        Long memberId = 1L;
        // 회원의 전체 계좌 조회 시 테스트 계좌 리스트 반환하도록 설정
        when(accountMapper.selectAccountsByUserId(memberId)).thenReturn(testAccounts);
        // 마지막 거래일이 없다고 설정
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);
        // 카테고리 분류 완료 설정
        when(categoryService.categorizeTransactions(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Static 메서드 모킹
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            // API 호출 시 테스트 거래 내역 반환
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(testTransactions);

            // when: null 요청으로 메서드 호출 (전체 계좌 처리)
            asyncTransactionService.getTransactions(memberId, null);

            // then: 결과 검증
            // 전체 계좌 조회가 1번 호출되었는지 확인
            verify(accountMapper, times(1)).selectAccountsByUserId(memberId);
            // 특정 계좌 조회는 호출되지 않았는지 확인
            verify(accountMapper, never()).getAccountByAccountNumber(anyString());
            // 나머지 처리 과정들이 올바르게 호출되었는지 확인
            verify(transactionService, times(1)).saveTransactionList(eq(memberId), eq(testAccount), eq(testTransactions));
            verify(categoryService, times(1)).categorizeTransactions(testTransactions);
            verify(summaryBatchService, times(1)).initDailySummary(memberId, testAccount);
        }
    }

    /**
     * 요청 DTO의 계좌번호가 빈 문자열인 경우 전체 계좌 조회 기능 테스트
     */
    @Test
    @DisplayName("거래 내역 조회 - 전체 계좌 (빈 계좌번호)")
    void getTransactions_AllAccounts_BlankAccount() throws IOException, InterruptedException {
        // given: 빈 계좌번호로 요청 DTO 생성
        Long memberId = 1L;
        TransactionRequestDto blankRequest = TransactionRequestDto.builder()
                .resAccount("") // 빈 계좌번호 - 전체 계좌 조회 트리거
                .build();

        // Mock 객체 동작 설정
        when(accountMapper.selectAccountsByUserId(memberId)).thenReturn(testAccounts);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);
        when(categoryService.categorizeTransactions(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Static 메서드 모킹
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(testTransactions);

            // when: 빈 계좌번호로 메서드 호출
            asyncTransactionService.getTransactions(memberId, blankRequest);

            // then: 전체 계좌 조회 로직이 실행되었는지 검증
            verify(accountMapper, times(1)).selectAccountsByUserId(memberId); // 전체 계좌 조회
            verify(accountMapper, never()).getAccountByAccountNumber(anyString()); // 특정 계좌 조회 안됨
        }
    }

    /**
     * 기존 거래 내역이 존재하는 계좌의 거래 내역 존재 여부 확인 기능 테스트
     * 증분 업데이트를 위해 마지막 거래일 이후부터 조회하도록 시작일을 조정하는 로직 검증
     */
    @Test
    @DisplayName("거래 내역 존재 여부 확인 - 기존 거래 내역 존재")
    void checkIsPresent_ExistingTransactions() {
        // given: 기존 거래 내역이 있는 상황 설정
        Long memberId = 1L;
        LocalDate lastDate = LocalDate.of(2024, 1, 15); // 마지막 거래일: 2024년 1월 15일
        TransactionRequestDto request = new TransactionRequestDto(); // 빈 요청 DTO

        // 해당 계좌의 마지막 거래일을 반환하도록 설정
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(lastDate);

        // when: 거래 내역 존재 여부 확인 메서드 호출
        asyncTransactionService.checkIsPresent(memberId, testAccount, request);

        // then: 결과 검증
        // 마지막 거래일 다음날(1월 16일)부터 조회하도록 시작일이 설정되었는지 확인
        assertEquals("20240116", request.getStartDate()); // 다음날부터 시작 (yyyyMMdd 형식)
        // 마지막 거래일 조회가 1번 호출되었는지 확인
        verify(transactionMapper, times(1)).getLastTransactionDate(memberId, testAccount.getResAccount());
    }

    /**
     * 기존 거래 내역이 없는 계좌의 거래 내역 존재 여부 확인 기능 테스트
     * 처음 연동하는 계좌이거나 거래 내역이 전혀 없는 경우의 동작 검증
     */
    @Test
    @DisplayName("거래 내역 존재 여부 확인 - 기존 거래 내역 없음")
    void checkIsPresent_NoExistingTransactions() {
        // given: 기존 거래 내역이 없는 상황 설정
        Long memberId = 1L;
        TransactionRequestDto request = new TransactionRequestDto(); // 빈 요청 DTO

        // 마지막 거래일이 없다고 설정 (null 반환)
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);

        // when: 거래 내역 존재 여부 확인 메서드 호출
        asyncTransactionService.checkIsPresent(memberId, testAccount, request);

        // then: 결과 검증
        // 시작일이 변경되지 않았는지 확인 (기존 거래 내역이 없으므로 기본 조회 기간 사용)
        assertNull(request.getStartDate()); // 변경되지 않음
        // 마지막 거래일 조회는 수행되었는지 확인
        verify(transactionMapper, times(1)).getLastTransactionDate(memberId, testAccount.getResAccount());
    }

    /**
     * null 요청으로 거래 내역 조회용 DTO를 생성하는 기능 테스트
     * 기본값 설정 로직 (2년 전부터 오늘까지, 최신순 정렬) 검증
     */
    @Test
    @DisplayName("거래 내역 DTO 생성 - null request")
    void makeTransactionDTO_NullRequest() {
        // given: null 요청 (기본값 설정 테스트)
        
        // when: null 요청으로 DTO 생성 메서드 호출
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, null);

        // then: 결과 검증
        assertNotNull(result); // 결과 DTO가 생성되었는지 확인
        // 계좌 정보가 올바르게 설정되었는지 확인
        assertEquals(testAccount.getResAccount(), result.getAccount()); // 계좌번호
        assertEquals(testAccount.getOrganization(), result.getOrganization()); // 금융기관
        assertEquals(testAccount.getConnectedId(), result.getConnectedId()); // 연동 ID
        assertEquals("0", result.getOrderBy()); // 기본 정렬 순서 (최신순)
        // 기본 조회 기간이 설정되었는지 확인
        assertNotNull(result.getStartDate()); // 시작일 (2년 전부터)
        assertNotNull(result.getEndDate()); // 종료일 (오늘까지)
    }

    /**
     * 완전한 요청 정보로 거래 내역 조회용 DTO를 생성하는 기능 테스트
     * 요청된 모든 매개변수가 그대로 DTO에 반영되는지 검증
     */
    @Test
    @DisplayName("거래 내역 DTO 생성 - 완전한 request")
    void makeTransactionDTO_CompleteRequest() {
        // given: 완전한 요청 정보 (모든 필드 설정됨)
        
        // when: 완전한 요청으로 DTO 생성 메서드 호출
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, testRequest);

        // then: 결과 검증
        assertNotNull(result); // 결과 DTO 생성 확인
        // 계좌 정보가 올바르게 설정되었는지 확인
        assertEquals(testAccount.getResAccount(), result.getAccount());
        assertEquals(testAccount.getOrganization(), result.getOrganization());
        assertEquals(testAccount.getConnectedId(), result.getConnectedId());
        // 요청 정보가 그대로 반영되었는지 확인
        assertEquals(testRequest.getOrderBy(), result.getOrderBy()); // 요청된 정렬 순서
        assertEquals(testRequest.getStartDate(), result.getStartDate()); // 요청된 시작일
        assertEquals(testRequest.getEndDate(), result.getEndDate()); // 요청된 종료일
    }

    /**
     * 부분적인 요청 정보 (시작일 누락)로 DTO를 생성하는 기능 테스트
     * 누락된 필드에 기본값이 설정되는지 검증
     */
    @Test
    @DisplayName("거래 내역 DTO 생성 - 부분적 request (startDate 누락)")
    void makeTransactionDTO_PartialRequest_MissingStartDate() {
        // given: 시작일이 없는 부분적인 요청 DTO 생성
        TransactionRequestDto partialRequest = TransactionRequestDto.builder()
                .resAccount("123456789") // 계좌번호만 설정
                .endDate("20240131") // 종료일만 설정
                .orderBy("0") // 정렬 순서만 설정
                .build(); // 시작일은 누락

        // when: 부분적인 요청으로 DTO 생성 메서드 호출
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, partialRequest);

        // then: 결과 검증
        assertNotNull(result); // 결과 DTO 생성 확인
        assertEquals(testAccount.getResAccount(), result.getAccount()); // 계좌 정보 확인
        assertNotNull(result.getStartDate()); // 시작일이 기본값으로 설정되었는지 확인 (2년 전)
        assertEquals(partialRequest.getEndDate(), result.getEndDate()); // 설정된 종료일 유지
        assertEquals(partialRequest.getOrderBy(), result.getOrderBy()); // 설정된 정렬 순서 유지
    }

    /**
     * 부분적인 요청 정보 (종료일 누락)로 DTO를 생성하는 기능 테스트
     */
    @Test
    @DisplayName("거래 내역 DTO 생성 - 부분적 request (endDate 누락)")
    void makeTransactionDTO_PartialRequest_MissingEndDate() {
        // given: 종료일이 없는 부분적인 요청 DTO 생성
        TransactionRequestDto partialRequest = TransactionRequestDto.builder()
                .resAccount("123456789")
                .startDate("20240101") // 시작일만 설정
                .orderBy("0")
                .build(); // 종료일은 누락

        // when: 부분적인 요청으로 DTO 생성 메서드 호출
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, partialRequest);

        // then: 결과 검증
        assertNotNull(result);
        assertEquals(testAccount.getResAccount(), result.getAccount());
        assertEquals(partialRequest.getStartDate(), result.getStartDate()); // 설정된 시작일 유지
        assertNotNull(result.getEndDate()); // 종료일이 기본값으로 설정되었는지 확인 (오늘)
        assertEquals(partialRequest.getOrderBy(), result.getOrderBy());
    }

    /**
     * 부분적인 요청 정보 (정렬 순서 누락)로 DTO를 생성하는 기능 테스트
     */
    @Test
    @DisplayName("거래 내역 DTO 생성 - 부분적 request (orderBy 누락)")
    void makeTransactionDTO_PartialRequest_MissingOrderBy() {
        // given: 정렬 순서가 없는 부분적인 요청 DTO 생성
        TransactionRequestDto partialRequest = TransactionRequestDto.builder()
                .resAccount("123456789")
                .startDate("20240101")
                .endDate("20240131")
                .build(); // 정렬 순서는 누락

        // when: 부분적인 요청으로 DTO 생성 메서드 호출
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, partialRequest);

        // then: 결과 검증
        assertNotNull(result);
        assertEquals(testAccount.getResAccount(), result.getAccount());
        assertEquals(partialRequest.getStartDate(), result.getStartDate());
        assertEquals(partialRequest.getEndDate(), result.getEndDate());
        assertEquals("0", result.getOrderBy()); // 정렬 순서가 기본값("0", 최신순)으로 설정되었는지 확인
    }

    /**
     * CODEF API 호출 중 IOException이 발생했을 때의 예외 처리 테스트
     * 네트워크 오류나 API 서버 문제 시나리오 검증
     */
    @Test
    @DisplayName("거래 내역 조회 - IOException 발생")
    void getTransactions_IOExceptionThrown() throws IOException, InterruptedException {
        // given: IOException이 발생하는 상황 설정
        Long memberId = 1L;
        when(accountMapper.getAccountByAccountNumber(testRequest.getResAccount()))
                .thenReturn(testAccount);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);

        // Static 메서드에서 IOException 발생하도록 설정
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenThrow(new IOException("API 호출 실패")); // IOException 발생

            // when & then: 예외 발생 확인
            assertThrows(RuntimeException.class, () -> {
                asyncTransactionService.getTransactions(memberId, testRequest); // RuntimeException으로 래핑되어 발생해야 함
            });

            // 예외 발생으로 후속 처리가 수행되지 않았는지 검증
            verify(transactionService, never()).saveTransactionList(any(), any(), any()); // 저장 안됨
            verify(categoryService, never()).categorizeTransactions(any()); // 카테고리 분류 안됨
            verify(summaryBatchService, never()).initDailySummary(any(), any()); // 집계 업데이트 안됨
        }
    }

    /**
     * CODEF API 호출 중 InterruptedException이 발생했을 때의 예외 처리 테스트
     * 스레드 중단이나 타임아웃 시나리오 검증
     */
    @Test
    @DisplayName("거래 내역 조회 - InterruptedException 발생")
    void getTransactions_InterruptedExceptionThrown() throws IOException, InterruptedException {
        // given: InterruptedException이 발생하는 상황 설정
        Long memberId = 1L;
        when(accountMapper.getAccountByAccountNumber(testRequest.getResAccount()))
                .thenReturn(testAccount);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount()))
                .thenReturn(null);

        // Static 메서드에서 InterruptedException 발생하도록 설정
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenThrow(new InterruptedException("스레드 중단")); // InterruptedException 발생

            // when & then: 예외 발생 확인
            assertThrows(RuntimeException.class, () -> {
                asyncTransactionService.getTransactions(memberId, testRequest); // RuntimeException으로 래핑되어 발생해야 함
            });

            // 예외 발생으로 후속 처리가 수행되지 않았는지 검증
            verify(transactionService, never()).saveTransactionList(any(), any(), any());
            verify(categoryService, never()).categorizeTransactions(any());
            verify(summaryBatchService, never()).initDailySummary(any(), any());
        }
    }
}
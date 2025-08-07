package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.service.CategoryService;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit5와 Mockito를 사용하여 AsyncTransactionServiceImpl의 통합 테스트를 수행하는 클래스입니다.
 * 외부 API 호출(TransactionResponse.requestTransactions)을 포함한 전체 비동기 흐름을 검증합니다.
 * @Async 동작은 Mockito 환경에서 동기적으로 실행되므로, 로직의 순차적 흐름을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncTransactionService 통합 테스트")
class AsyncTransactionServiceTest {

    // @Mock: 각 의존성의 모의 객체를 생성합니다.
    @Mock private TransactionMapper transactionMapper;
    @Mock private AccountMapper accountMapper;
    @Mock private TransactionService transactionService;
    @Mock private CategoryService categoryService;
    @Mock private SummaryBatchService summaryBatchService;
    @Mock private RedisService redisService;

    // @InjectMocks: 모의 객체들을 AsyncTransactionServiceImpl에 주입하여 테스트 대상을 생성합니다.
    @InjectMocks
    private AsyncTransactionServiceImpl asyncTransactionService;

    // 테스트에 사용될 공통 데이터입니다.
    private Long memberId;
    private AccountVO testAccount;
    private List<AccountVO> testAccounts;
    private List<TransactionHistoryVO> testTransactions;
    private TransactionRequestDto testRequest;

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
                .organization("KB")
                .connectedId("test-connected-id")
                .build();

        testAccounts = Collections.singletonList(testAccount);

        // 테스트용 거래 내역 리스트를 생성합니다.
        testTransactions = Collections.singletonList(
                TransactionHistoryVO.builder().id(1L).description("Test Transaction").build()
        );

        // 테스트용 요청 DTO를 생성합니다.
        testRequest = TransactionRequestDto.builder()
                .resAccount(testAccount.getResAccount())
                .startDate("20240101")
                .endDate("20240131")
                .orderBy("0")
                .build();
    }

    /**
     * 특정 계좌의 거래 내역을 성공적으로 조회하는 전체 흐름을 테스트합니다.
     */
    @Test
    @DisplayName("거래 내역 조회 - 특정 계좌 지정 성공")
    void getTransactions_SpecificAccount_Success() {
        // given: 특정 계좌 조회 시, 모의 객체들이 반환할 데이터를 설정합니다.
        when(accountMapper.getAccountByAccountNumber(testRequest.getResAccount())).thenReturn(testAccount);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(null);
        doNothing().when(categoryService).categorizeTransactions(any(), anyString());

        // TransactionResponse의 정적(static) 메서드인 requestTransactions를 모의 처리합니다.
        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(testTransactions);

            // when: 테스트 대상 메서드를 호출합니다.
            asyncTransactionService.getTransactions(memberId, testRequest);

            // then: 각 의존성 메서드가 예상된 파라미터로 정확히 1번씩 호출되었는지 검증합니다.
            verify(accountMapper, times(1)).getAccountByAccountNumber(testRequest.getResAccount());
            verify(accountMapper, never()).selectAccountsByUserId(anyLong());
            verify(transactionService, times(1)).saveTransactionList(memberId, testAccount, testTransactions);
            verify(categoryService, times(1)).categorizeTransactions(eq(testTransactions), anyString());
            verify(summaryBatchService, times(1)).initDailySummary(memberId, testAccount, anyString());
        }
    }

    /**
     * 전체 계좌의 거래 내역을 성공적으로 조회하는 흐름을 테스트합니다. (요청 DTO가 null인 경우)
     */
    @Test
    @DisplayName("거래 내역 조회 - 전체 계좌 성공 (요청이 null)")
    void getTransactions_AllAccounts_NullRequest_Success() {
        // given: 전체 계좌 조회 시, 모의 객체들이 반환할 데이터를 설정합니다.
        when(accountMapper.selectAccountsByUserId(memberId)).thenReturn(testAccounts);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(null);
        
        // [수정] categorizeTransactions는 2개의 인자를 받으므로, any()와 anyString()으로 모의 설정합니다.
        doNothing().when(categoryService).categorizeTransactions(any(), anyString());

        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            mockedStatic.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(testTransactions);

            // when: 요청 DTO를 null로 하여 메서드를 호출합니다.
            asyncTransactionService.getTransactions(memberId, null);

            // then: 전체 계좌 조회 흐름에 맞게 메서드들이 호출되었는지 검증합니다.
            verify(accountMapper, times(1)).selectAccountsByUserId(memberId);
            verify(accountMapper, never()).getAccountByAccountNumber(anyString());
            verify(transactionService, times(1)).saveTransactionList(memberId, testAccount, testTransactions);
            
            // [수정] categorizeTransactions 호출을 2개의 인자로 검증합니다.
            verify(categoryService, times(1)).categorizeTransactions(eq(testTransactions), anyString());
            verify(summaryBatchService, times(1)).initDailySummary(memberId, testAccount, anyString());
        }
    }

    /**
     * 외부 API 호출 중 IOException이 발생했을 때, 예외가 올바르게 처리되는지 테스트합니다.
     */
    @Test
    @DisplayName("거래 내역 조회 - API 호출 시 IOException 발생")
    void getTransactions_ThrowsIOException() {
        // given: 계좌 조회 및 마지막 거래일 조회는 정상적으로 동작하도록 설정합니다.
        when(accountMapper.getAccountByAccountNumber(testRequest.getResAccount())).thenReturn(testAccount);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(null);

        try (MockedStatic<TransactionResponse> mockedStatic = mockStatic(TransactionResponse.class)) {
            // given: 외부 API 호출 시 IOException이 발생하도록 설정합니다.
            mockedStatic.when(() -> TransactionResponse.requestTransactions(anyLong(), any(TransactionDTO.class)))
                    .thenThrow(new IOException("API Call Failed"));

            // when & then: RuntimeException이 발생하는지 검증합니다.
            assertThrows(RuntimeException.class, () -> {
                asyncTransactionService.getTransactions(memberId, testRequest);
            });

            // then: 예외 발생 후, 후속 처리(저장, 분류, 집계)가 호출되지 않았는지 검증합니다.
            verify(transactionService, never()).saveTransactionList(any(), any(), any());
            verify(categoryService, never()).categorizeTransactions(any(), anyString());
            verify(summaryBatchService, never()).initDailySummary(any(), any(),anyString());
        }
    }

    @Test
    @DisplayName("증분 업데이트 - 마지막 거래일 다음날부터 조회")
    void checkIsPresent_IncrementalUpdate() {
        // given: 마지막 거래일이 존재하는 상황을 설정합니다.
        LocalDate lastDate = LocalDate.of(2024, 8, 1);
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(lastDate);
        TransactionRequestDto request = new TransactionRequestDto();

        // when: 증분 업데이트 여부를 확인하는 메서드를 호출합니다.
        asyncTransactionService.checkIsPresent(memberId, testAccount, request);

        // then: 요청 DTO의 시작일이 마지막 거래일의 다음날로 설정되었는지 검증합니다.
        assertEquals("20240802", request.getStartDate());
    }

    @Test
    @DisplayName("초기 조회 - 마지막 거래일 없음")
    void checkIsPresent_InitialFetch() {
        // given: 마지막 거래일이 존재하지 않는 상황(null)을 설정합니다.
        when(transactionMapper.getLastTransactionDate(memberId, testAccount.getResAccount())).thenReturn(null);
        TransactionRequestDto request = new TransactionRequestDto();

        // when: 증분 업데이트 여부를 확인하는 메서드를 호출합니다.
        asyncTransactionService.checkIsPresent(memberId, testAccount, request);

        // then: 요청 DTO의 시작일이 변경되지 않았는지(null) 검증합니다.
        assertNull(request.getStartDate());
    }

    @Test
    @DisplayName("거래 내역 DTO 생성 - 요청이 null일 때 기본값 설정")
    void makeTransactionDTO_NullRequest() {
        // when: 요청 DTO가 null일 때, 거래 내역 DTO를 생성합니다.
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, null);

        // then: DTO가 기본값(조회 기간: 2년, 정렬: 최신순)으로 올바르게 생성되었는지 검증합니다.
        assertNotNull(result);
        assertEquals(testAccount.getResAccount(), result.getAccount());
        assertEquals(LocalDate.now().minusYears(2).toString().replace("-", ""), result.getStartDate());
        assertEquals(LocalDate.now().toString().replace("-", ""), result.getEndDate());
        assertEquals("0", result.getOrderBy());
    }

    @Test
    @DisplayName("거래 내역 DTO 생성 - 요청 값이 있을 때 그대로 사용")
    void makeTransactionDTO_WithRequest() {
        // when: 완전한 요청 DTO로 거래 내역 DTO를 생성합니다.
        TransactionDTO result = asyncTransactionService.makeTransactionDTO(testAccount, testRequest);

        // then: DTO가 요청 DTO의 값으로 올바르게 생성되었는지 검증합니다.
        assertNotNull(result);
        assertEquals(testRequest.getResAccount(), result.getAccount());
        assertEquals(testRequest.getStartDate(), result.getStartDate());
        assertEquals(testRequest.getEndDate(), result.getEndDate());
        assertEquals(testRequest.getOrderBy(), result.getOrderBy());
    }
}
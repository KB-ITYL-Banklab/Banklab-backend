package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.service.CategoryService;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AsyncTransactionServiceImpl에 대한 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class AsyncTransactionServiceImplTest {

    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private SummaryBatchService summaryBatchService;
    @Mock
    private RedisService redisService;

    @InjectMocks
    private AsyncTransactionServiceImpl asyncTransactionService;

    // TransactionResponse.requestTransactions 정적 메소드를 mock하기 위함
    private MockedStatic<TransactionResponse> mockedTransactionResponse;

    @BeforeEach
    void setUp() {
        // 각 테스트 실행 전에 정적 메소드 mocking을 시작
        mockedTransactionResponse = mockStatic(TransactionResponse.class);
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 실행 후에 mocking을 종료하여 다른 테스트에 영향이 없도록 함
        mockedTransactionResponse.close();
    }

    @Nested
    @DisplayName("getTransactions 메소드 테스트")
    class GetTransactionsTest {

        private final long memberId = 1L;
        private final String accountNumber = "110-220-334455";
        private final String key = RedisKeyUtil.transaction(memberId, accountNumber);

        @Test
        @DisplayName("요청이 null일 경우 IllegalArgumentException을 발생시켜야 한다")
        void getTransactions_WhenRequestIsNull_ShouldThrowException() {
            // given
            TransactionRequestDto request = null;

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                asyncTransactionService.getTransactions(memberId, request);
            });
        }

        @Test
        @DisplayName("이미 처리 중인 계좌일 경우, 작업을 시작하지 않고 조기 종료해야 한다")
        void getTransactions_WhenAlreadyProcessing_ShouldReturnEarly() {
            // given
            TransactionRequestDto request = new TransactionRequestDto();
            request.setResAccount(accountNumber);
            // setIfAbsent가 true를 반환하면 이미 키가 존재함을 의미 (처리 중)
            when(redisService.setIfAbsent(eq(key), anyString(), any(Duration.class))).thenReturn(true);

            // when
            asyncTransactionService.getTransactions(memberId, request);

            // then
            // 이미 처리 중이므로 계좌 조회 등 다음 로직이 호출되지 않아야 함
            verify(accountMapper, never()).getAccountByAccountNumber(anyString());
        }

        @Test
        @DisplayName("계좌 정보가 없을 경우, 작업을 중단하고 조기 종료해야 한다")
        void getTransactions_WhenAccountNotFound_ShouldReturnEarly() {
            // given
            TransactionRequestDto request = new TransactionRequestDto();
            request.setResAccount(accountNumber);
            when(redisService.setIfAbsent(eq(key), anyString(), any(Duration.class))).thenReturn(false);
            // 계좌 조회 결과가 null
            when(accountMapper.getAccountByAccountNumber(accountNumber)).thenReturn(null);

            // when
            asyncTransactionService.getTransactions(memberId, request);

            // then
            // 계좌가 없으므로 거래 내역 조회 등 다음 로직이 호출되지 않아야 함
            verify(transactionMapper, never()).getLastTransactionDate(anyLong(), anyString());
            mockedTransactionResponse.verify(() -> TransactionResponse.requestTransactions(anyLong(), any(TransactionDTO.class)), never());
        }

        @Test
        @DisplayName("모든 조건이 충족될 경우, 전체 프로세스를 성공적으로 실행해야 한다")
        void getTransactions_WhenHappyPath_ShouldExecuteAllSteps() throws IOException, InterruptedException {
            // given
            TransactionRequestDto request = new TransactionRequestDto();
            request.setResAccount(accountNumber);
            AccountVO account = new AccountVO();
            account.setResAccount(accountNumber);
            List<TransactionHistoryVO> transactions = Collections.singletonList(new TransactionHistoryVO());

            // Mocking a successful flow
            when(redisService.setIfAbsent(eq(key), anyString(), any(Duration.class))).thenReturn(false);
            when(accountMapper.getAccountByAccountNumber(accountNumber)).thenReturn(account);
            when(transactionMapper.getLastTransactionDate(memberId, accountNumber)).thenReturn(LocalDate.now().minusDays(10));
            mockedTransactionResponse.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(transactions);
            when(redisService.tryLock(anyString(), anyString(),anyInt())).thenReturn(true);

            // when
            asyncTransactionService.getTransactions(memberId, request);

            // then
            // 각 단계의 서비스가 순서대로 호출되었는지 검증
            verify(transactionService).saveTransactionList(memberId, account, transactions);
            verify(categoryService).categorizeTransactions(eq(transactions), eq(key));
            verify(summaryBatchService).initDailySummary(eq(memberId), eq(account), anyString());
            verify(redisService).set(key, "DONE", 1);
            verify(redisService).unlock(anyString(), anyString());
        }

        @Test
        @DisplayName("카테고리 분류 중 예외 발생 시, Redis 상태를 FAILED로 설정해야 한다")
        void getTransactions_WhenCategorizationFails_ShouldSetRedisToFailed() throws IOException, InterruptedException {
            // given
            TransactionRequestDto request = new TransactionRequestDto();
            request.setResAccount(accountNumber);
            AccountVO account = new AccountVO();
            account.setResAccount(accountNumber);
            List<TransactionHistoryVO> transactions = Collections.singletonList(new TransactionHistoryVO());

            when(redisService.setIfAbsent(eq(key), anyString(), any(Duration.class))).thenReturn(false);
            when(accountMapper.getAccountByAccountNumber(accountNumber)).thenReturn(account);
            mockedTransactionResponse.when(() -> TransactionResponse.requestTransactions(eq(memberId), any(TransactionDTO.class)))
                    .thenReturn(transactions);
            // 카테고리 분류 시 예외 발생 mocking
            doThrow(new RuntimeException("Category service error")).when(categoryService).categorizeTransactions(anyList(), anyString());

            // when
            asyncTransactionService.getTransactions(memberId, request);

            // then
            // 집계 서비스는 호출되지 않아야 함
            verify(summaryBatchService, never()).initDailySummary(anyLong(), any(AccountVO.class), anyString());
            // Redis 상태가 FAILED로 설정되어야 함
            verify(redisService).set(key, "FAILED", 1);
        }
    }

    @Nested
    @DisplayName("makeTransactionDTO 메소드 테스트")
    class MakeTransactionDTOTest {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        @Test
        @DisplayName("요청 DTO가 null일 경우, 기본값으로 DTO를 생성해야 한다")
        void makeTransactionDTO_WithNullRequest_ShouldCreateDefaultDTO() {
            // given
            AccountVO account = new AccountVO();
            account.setResAccount("123");
            account.setOrganization("004");
            account.setConnectedId("conn-id");
            TransactionRequestDto request = null;

            // when
            TransactionDTO dto = asyncTransactionService.makeTransactionDTO(account, request);

            // then
            assertThat(dto.getAccount()).isEqualTo("123");
            assertThat(dto.getOrganization()).isEqualTo("004");
            assertThat(dto.getConnectedId()).isEqualTo("conn-id");
            assertThat(dto.getOrderBy()).isEqualTo("0");
            assertThat(dto.getStartDate()).isEqualTo(LocalDate.now().minusYears(2).format(formatter));
            assertThat(dto.getEndDate()).isEqualTo(LocalDate.now().format(formatter));
        }

        @Test
        @DisplayName("요청 DTO의 일부 필드가 비어있을 경우, 해당 필드를 기본값으로 채워야 한다")
        void makeTransactionDTO_WithPartialRequest_ShouldFillDefaults() {
            // given
            AccountVO account = new AccountVO();
            account.setResAccount("123");
            TransactionRequestDto request = new TransactionRequestDto();
            request.setStartDate("20230101"); // 시작일만 제공

            // when
            TransactionDTO dto = asyncTransactionService.makeTransactionDTO(account, request);

            // then
            assertThat(dto.getStartDate()).isEqualTo("20230101");
            assertThat(dto.getEndDate()).isEqualTo(LocalDate.now().format(formatter)); // 종료일은 기본값
            assertThat(dto.getOrderBy()).isEqualTo("0"); // 정렬 순서는 기본값
        }
    }
}

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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class AsyncTransactionServiceImplTest {
    @InjectMocks
    private AsyncTransactionServiceImpl asyncTransactionService;

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
    @Mock
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ checkIsPresent() 단위 테스트
    @Test
    void checkIsPresent_setsStartDateIfExists() {
        Long memberId = 1L;
        AccountVO account = new AccountVO();
        account.setResAccount("1234567890");

        LocalDate lastDate = LocalDate.of(2024, 12, 1);
        when(transactionMapper.getLastTransactionDate(memberId, account.getResAccount())).thenReturn(lastDate);

        TransactionRequestDto dto = new TransactionRequestDto();
        asyncTransactionService.checkIsPresent(memberId, account, dto);

        assertEquals("20241201", dto.getStartDate());
    }

    @Test
    void checkIsPresent_doesNothingIfNoDate() {
        Long memberId = 1L;
        AccountVO account = new AccountVO();
        account.setResAccount("1234567890");

        when(transactionMapper.getLastTransactionDate(memberId, account.getResAccount())).thenReturn(null);

        TransactionRequestDto dto = new TransactionRequestDto();
        asyncTransactionService.checkIsPresent(memberId, account, dto);

        assertNull(dto.getStartDate());
    }

    // ✅ makeTransactionDTO() 단위 테스트
    @Test
    void makeTransactionDTO_setsDefaultsIfRequestIsNull() {
        AccountVO account = new AccountVO();
        account.setResAccount("123");
        account.setConnectedId("conn");
        account.setOrganization("ORG");

        TransactionDTO dto = asyncTransactionService.makeTransactionDTO(account, null);

        assertNotNull(dto.getStartDate());
        assertNotNull(dto.getEndDate());
        assertEquals("0", dto.getOrderBy());
        assertEquals("123", dto.getAccount());
    }

    @Test
    void makeTransactionDTO_usesProvidedValues() {
        AccountVO account = new AccountVO();
        account.setResAccount("123");
        account.setConnectedId("conn");
        account.setOrganization("ORG");

        TransactionRequestDto req = new TransactionRequestDto();
        req.setStartDate("20220101");
        req.setEndDate("20230101");
        req.setOrderBy("1");

        TransactionDTO dto = asyncTransactionService.makeTransactionDTO(account, req);

        assertEquals("20220101", dto.getStartDate());
        assertEquals("20230101", dto.getEndDate());
        assertEquals("1", dto.getOrderBy());
    }

    // ✅ getTransactions()에 대한 통합 시뮬레이션 테스트 (happy path)
    @Test
    void getTransactions_runsSuccessfully() throws Exception {
        long memberId = 1L;
        String accountNum = "1234567890";
        TransactionRequestDto req = new TransactionRequestDto();
        req.setResAccount(accountNum);

        AccountVO account = new AccountVO();
        account.setResAccount(accountNum);
        account.setConnectedId("cid");
        account.setOrganization("ORG");

        List<TransactionHistoryVO> txList = List.of(new TransactionHistoryVO());

        // Redis 락 없다고 가정
        when(redisService.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);
        when(accountMapper.getAccountByAccountNumber(accountNum)).thenReturn(account);
        when(transactionResponse.requestTransactions(eq(memberId), any())).thenReturn(txList);

        // void 메서드 stubbing
        doNothing().when(transactionService).saveTransactionList(eq(memberId), eq(account), eq(txList));
        doNothing().when(categoryService).categorizeTransactions(eq(txList), anyString());
        doNothing().when(summaryBatchService).initDailySummary(eq(memberId),  any());

        // when
        asyncTransactionService.getTransactions(memberId, req);

        // then
        verify(transactionService).saveTransactionList(eq(memberId), eq(account), eq(txList));
        verify(categoryService).categorizeTransactions(eq(txList), anyString());
        verify(summaryBatchService).initDailySummary(eq(memberId), any());
        verify(redisService).set(anyString(), eq("DONE"), eq(1));
    }
}
package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.category.service.CategoryService;
import com.banklab.codef.util.ApiRequest;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.TransactionDetailDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@Log4j2
@Transactional // 트랜잭션 롤백을 위해 사용
@ContextConfiguration(classes = {AsyncTransactionServiceImplConcurrencyTest.TestConfig.class}) // 필요한 Spring 설정 클래스 로드
@DisplayName("AsyncTransactionServiceImpl 동시성 통합 테스트")
class AsyncTransactionServiceImplConcurrencyTest {

    @Autowired
    private AsyncTransactionService asyncTransactionService;
    @MockBean // RedisService는 실제 DB 접근 대신 Mocking
    private RedisService redisService;
    @MockBean 
    private AccountMapper accountMapper;
    @MockBean 
    private TransactionMapper transactionMapper;
    @MockBean 
    private ApiRequest apiRequest;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private SummaryBatchService summaryBatchService;

    private static final long MEMBER_ID = 10051L;
    private static final List<String> TEST_ACCOUNTS = List.of(
            "1111111111", "2222222222", "3333333333", "4444444444",
            "5555555555", "6666666666", "7777777777", "8888888888",
            "9999999999", "0000000000"
    );

    private TransactionRequestDto makeRequestDto(String account) {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.setResAccount(account);
        dto.setStartDate("20250101");
        dto.setEndDate("20250807");
        return dto;
    }

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        // RedisService는 MockBean이므로, Mocking 설정
        // 실제 RedisService의 delete 메서드는 호출되지 않으므로, Mocking할 필요 없음

        // accountMapper Mocking: 각 테스트 계좌에 대해 AccountVO 반환
        for (String account : TEST_ACCOUNTS) {
            AccountVO accountVO = new AccountVO();
            accountVO.setResAccount(account);
            accountVO.setConnectedId("testConnectedId_" + account);
            accountVO.setOrganization("testOrg_" + account);
            when(accountMapper.getAccountByAccountNumber(eq(account))).thenReturn(accountVO);
        }

        // transactionMapper Mocking: getLastTransactionDate는 null 반환, getTransactionDetailsByAccountId는 빈 리스트 반환
        when(transactionMapper.getLastTransactionDate(anyLong(), anyString())).thenReturn(null);
        when(transactionMapper.getTransactionDetailsByAccountId(anyLong(), anyString(), any(), any())).thenReturn(Collections.emptyList());

        // ApiRequest Mocking: request 메서드가 더미 JSON 응답을 반환하도록 설정
        String dummyApiResponse = "{\"data\":{\"resAccount\":\"1111111111\",\"resTrHistoryList\":[{\"tranDate\":\"20250801\",\"tranTime\":\"100000\",\"tranAmt\":1000,\"tranType\":\"입금\",\"content\":\"테스트입금\"}]}}";
        when(apiRequest.request(anyString(), anyMap())).thenReturn(dummyApiResponse);

        // RedisService.setIfAbsent Mocking: 모든 호출에 대해 true를 반환하여 로직이 진행되도록 함
        when(redisService.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
    }

    @Test
    public void testMultipleAccountConcurrency() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(TEST_ACCOUNTS.size());
        CountDownLatch latch = new CountDownLatch(TEST_ACCOUNTS.size());

        for (String account : TEST_ACCOUNTS) {
            executor.execute(() -> {
                try {
                    asyncTransactionService.getTransactions(MEMBER_ID, makeRequestDto(account));
                } catch (Exception e) {
                    log.error("Error processing account " + account, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS); // 충분한 시간 대기
        executor.shutdown();

        // 모든 계좌에 대한 처리 결과 검증
        for (String account : TEST_ACCOUNTS) {
            String redisKey = RedisKeyUtil.transaction(MEMBER_ID, account);
            String redisValue = redisService.get(redisKey);
            assertEquals("DONE", redisValue, "계좌 " + account + " 처리 상태가 DONE이 아님");

            // 각 계좌별로 핵심 메서드들이 호출되었는지 검증
            verify(accountMapper, timeout(5000).times(1)).getAccountByAccountNumber(eq(account));
            verify(transactionMapper, timeout(5000).times(1)).getLastTransactionDate(eq(MEMBER_ID), eq(account));
            verify(transactionService, timeout(5000).times(1)).saveTransactionList(eq(MEMBER_ID), any(AccountVO.class), anyList());
            verify(categoryService, timeout(5000).times(1)).categorizeTransactions(anyList(), eq(redisKey));
            verify(redisService, timeout(5000).times(1)).setIfAbsent(eq(redisKey), eq("FETCHING_TRANSACTIONS"), any(Duration.class));
            verify(redisService, timeout(5000).times(1)).set(eq(redisKey), eq("CLASSIFYING_CATEGORIES"), anyInt());
            verify(redisService, timeout(5000).times(1)).set(eq(redisKey), eq("ANALYZING_DATA"), anyInt());
            verify(redisService, timeout(5000).times(1)).set(eq(redisKey), eq("DONE"), anyInt());
        }
    }

    @Configuration
    @EnableAsync
    static class TestConfig {
        @Bean
        public AsyncTransactionServiceImpl asyncTransactionServiceImpl(
                TransactionMapper transactionMapper,
                AccountMapper accountMapper,
                TransactionService transactionService,
                CategoryService categoryService,
                SummaryBatchService summaryBatchService,
                RedisService redisService,
                TransactionResponse transactionResponse) {
            return new AsyncTransactionServiceImpl(
                    transactionMapper,
                    accountMapper,
                    transactionService,
                    categoryService,
                    summaryBatchService,
                    redisService,
                    transactionResponse);
        }

        @Bean
        public Executor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(10);
            executor.setQueueCapacity(25);
            executor.setThreadNamePrefix("AsyncTest-");
            executor.initialize();
            return executor;
        }
    }
}

package com.banklab.transaction.service;

import com.banklab.codeapi.config.RestTemplateConfig;
import com.banklab.config.RootConfig;
import com.banklab.config.ServletConfig;
import com.banklab.config.WebConfig;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, RestTemplateConfig.class})
@Transactional
@Log4j2
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionMapper  transactionMapper;

    @Test
    @DisplayName("월간 요약 정보 조회 테스트 - DB에 데이터가 없는 경우")
    void getMonthlySummary_whenNoDataInDB() {
        // given
        int year = 2024;
        int month = 7;
        // DB에 존재하지 않는 임의의 계좌번호
        String nonExistentAccount = "0000000000";

        // when
        MonthlySummaryDTO actualSummary = transactionService.getMonthlySummary(year, month, nonExistentAccount);

        // then
        assertNotNull(actualSummary);
        assertEquals(0, actualSummary.getTotalIncome());
        assertEquals(0, actualSummary.getTotalExpense());
    }

    @Test
    @DisplayName("월간 요약 정보 조회 테스트 - DB 데이터 존재")
    void getMonthlySummary_whenDataInDB() {
        // given
        int year = 2025;
        int month = 7;
        String ExistentAccount = "110568497184";

        long expectedTotalIncome = 870666L;
        long expectedTotalOutcome = 716479L;

        //when
        MonthlySummaryDTO monthlySummary = transactionService.getMonthlySummary(year, month, ExistentAccount);

        assertEquals(expectedTotalOutcome, monthlySummary.getTotalExpense());
        assertEquals(expectedTotalIncome, monthlySummary.getTotalIncome());
    }
}

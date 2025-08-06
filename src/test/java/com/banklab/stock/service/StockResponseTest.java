package com.banklab.stock.service;

import com.banklab.account.service.AccountService;
import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.security.config.SecurityConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.banklab.stock.service.StockResponse.requestStocks;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Log4j2
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class, RedisConfig.class})
class StockResponseTest {

    @Test
    void 증권계좌_불러오기_테스트() throws Exception {
        String connectedId = "8bRNzA0j4EG860L9rfB-Yo";
        String account = "7164752822-01";

        requestStocks(1L, "0240", connectedId, account, "");

    }
}
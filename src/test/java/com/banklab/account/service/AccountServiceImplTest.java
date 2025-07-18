package com.banklab.account.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Log4j2
@ContextConfiguration(classes = RootConfig.class)
class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    @Test
    void 계좌조회_테스트() throws Exception {
        String userId = "test_id";
        String connectedId = "";
        String organization = "0004";

        List<AccountVO> list = AccountResponse.requestAccounts(userId, organization, connectedId);

        accountService.saveAccounts(list);

        List<AccountDTO> accountDTOList = accountService.getUserAccounts(userId);

        for(AccountDTO accountDTO : accountDTOList) {
            log.info("보유 계좌: {}", accountDTO.getResAccountName());
            log.info("계좌번호: {}", accountDTO.getResAccountDisplay());
            log.info("잔액: {}",accountDTO.getResAccountBalance());
        }
    }

    @Test
    void updateBalanceTest() throws Exception {
        String userId = "user00";
        String connectedId = "";
        String organization = "0004";

        accountService.refreshAccountBalance(userId, organization, connectedId);

        List<AccountDTO> accountDTOList = accountService.getUserAccounts(userId);

        for(AccountDTO accountDTO : accountDTOList) {
            log.info("보유 계좌: {}", accountDTO.getResAccountName());
            log.info("계좌번호: {}", accountDTO.getResAccountDisplay());
            log.info("잔액: {}",accountDTO.getResAccountBalance());
        }
    }

    @Test
    void deleteAccountTest() throws Exception {
        String userId = "testuser001";
        String connectedId = "4ONbo5CU4nF8LzsXYg3nNH";

        accountService.deleteAccount(userId, connectedId);

    }

}
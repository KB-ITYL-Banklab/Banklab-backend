package com.banklab.account.service;

import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.banklab.account.service.AccountResponse.requestAccounts;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 계좌 연동을 하기 위해 CODEF 보유계좌 호출 테스트
 *
 * @Method 계좌_연동_테스트 : Codef API를 호출해서 계좌를 불러옵니다.
 */
@ExtendWith(SpringExtension.class)
@Log4j2
@ContextConfiguration(classes = RootConfig.class)
class AccountResponseTest {

    @Test
    void 계좌_연동_테스트() throws Exception {
        String connectedId = "발급된 커넥티드 아이디";

        requestAccounts(1L,"0004", connectedId);
    }
}

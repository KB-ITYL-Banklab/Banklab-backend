package com.banklab.account.service;

import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.banklab.account.service.AccountResponse.requestAccounts;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Log4j2
@ContextConfiguration(classes = RootConfig.class)
class AccountResponseTest {

    @Test
    void accountResponseTest() throws Exception {
        String connectedId = "1OXShkrgQ8JaO1K2rgiiOa";

        requestAccounts("test_id","0004", connectedId);
    }
}

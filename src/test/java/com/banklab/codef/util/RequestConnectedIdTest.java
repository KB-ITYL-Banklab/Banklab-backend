package com.banklab.codef.util;

import com.banklab.codef.service.RequestConnectedId;
import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Log4j2
@ContextConfiguration(classes = {RootConfig.class})
@ExtendWith(SpringExtension.class)
class RequestConnectedIdTest {

    @Test
    void create() throws Exception {
        String id = "kluyr1231";
        String password = "dkdlwmdnjs1@";

        String connected_id = RequestConnectedId.createConnectedId(id, password);
        log.info("Connected ID: " + connected_id);

        // 1OXShkrgQ8JaO1K2rgiiOa
    }
}
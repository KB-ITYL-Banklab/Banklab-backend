package com.banklab.codef.util;

import com.banklab.codef.service.RequestConnectedId;
import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 커넥티드 아이디 관련 테스트
 *
 * @Method 커넥티드_아이디_생성 : 커넥티드 아이디를 발급받습니다.
 * @Method 커넥티드_아이디_삭제 : 커넥티드 아이디를 삭제합니다.
 */
@Log4j2
@ContextConfiguration(classes = {RootConfig.class})
@ExtendWith(SpringExtension.class)
class RequestConnectedIdTest {

    @Test
    void 커넥티드_아이디_생성() throws Exception {
        String id = "bank_id"; // 은행 아이디
        String password = "bank_password"; // 은행 비밀번호

        String connected_id = RequestConnectedId.createConnectedId(id, password, "0004");
        log.info("Connected ID: " + connected_id);

    }

    @Test
    void 커넥티드_아이디_삭제() throws Exception {
        String connected_id = "connecte_id"; // 커넥티드 아이디
        String bankCode = "organization"; // 기관코드(은행코드 ex. 0004)

        RequestConnectedId.deleteConnectedId(connected_id, bankCode);

    }
}
package com.banklab.codef.util;

import com.banklab.codef.service.RequestConnectedId;
import com.banklab.config.MailConfig;
import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.security.config.SecurityConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 커넥티드 아이디 관련 테스트
 *
 * @Method 커넥티드_아이디_생성 : 커넥티드 아이디를 발급받습니다.
 * @Method 커넥티드_아이디_삭제 : 커넥티드 아이디를 삭제합니다.
 */
@Log4j2
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class, RedisConfig.class, CommonConstant.class, MailConfig.class})
@ExtendWith(SpringExtension.class)
class RequestConnectedIdTest {

    @Test
    void 커넥티드_아이디_생성() throws Exception {

        // 환경변수 값들이 제대로 로드되었는지 확인
        log.info("CLIENT_ID: " + CommonConstant.CLIENT_ID);
        log.info("SECRET_KEY: " + CommonConstant.SECRET_KEY);
        log.info("PUBLIC_KEY: " + CommonConstant.PUBLIC_KEY);

        String id = "alswo2833"; // 은행 아이디
        String password = "wjdwo8133@@"; // 은행 비밀번호

        // 아아디, 패스워드, 기관코드, 비즈니스 타입(은행 BK, 증권 ST, 카드 CD), 고객구분 [은행(개인) P, 증권(통합) A]
        String connected_id = RequestConnectedId.createConnectedId(id, password, "0240", "ST", "A");
        log.info("Connected ID: " + connected_id);

    }

    @Test
    void 커넥티드_아이디_삭제() throws Exception {
        String connected_id = "cdgK5d2sku0b5j6R4Xg6bW"; // 커넥티드 아이디
        String bankCode = "0240"; // 기관코드(은행코드 ex. 0004)

        RequestConnectedId.deleteConnectedId(connected_id, bankCode, "ST","A");

    }
}
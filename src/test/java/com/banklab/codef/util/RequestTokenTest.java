package com.banklab.codef.util;

import com.banklab.codef.service.RequestToken;
import com.banklab.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 액세스 토큰을 발급받기 위한 테스트 코드
 *
 * @Method getToken : 액세스 토큰 발급 요청
 */
@Log4j2
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class RequestTokenTest {

    // 수정 요망
    private String clientId = "6675c900-d0bf-4fd1-ba20-28387b613835";
    private String clientSecret = "5ca86438-4597-4878-b6bb-781f4100ce2f";

    @Test
    void getToken() throws Exception {
        String accessToken = RequestToken.getAccessToken(clientId, clientSecret);

        assertNotNull(accessToken, "Access Token이 null이면 안 됨.");
        assertFalse(accessToken.isEmpty(), "Access Token이 비어있으면 안 됨.");

        log.info("발급된 Access Token: " + accessToken);
    }
}
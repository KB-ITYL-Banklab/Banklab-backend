package com.banklab.security;

import com.banklab.config.RootConfig;
import com.banklab.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class})
@Slf4j
public class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder pwEncoder;

    @Test
    public void testEncode() {
        String str = "1234";

        String enStr = pwEncoder.encode(str);
        log.info("password: " + enStr);

        String enStr2 = pwEncoder.encode(str);
        log.info("password: " + enStr2);

        log.info("match :" + pwEncoder.matches(str, enStr)); // 비밀번호 일치 여부 검사
        log.info("match :" + pwEncoder.matches(str, enStr2));// 비밀번호 일치 여부 검사
    }
}
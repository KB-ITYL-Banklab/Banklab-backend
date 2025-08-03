package com.banklab.security.util;

import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.security.config.SecurityConfig;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class, RedisConfig.class })
@Log4j2
class JwtProcessorTest {

    @Autowired
    JwtProcessor jwtProcessor;

    @Test
    void generateToken() {
        // 테스트에 사용할 username
        String username = "user0";

        // username을 이용해 JWT 토큰 생성
        String token = jwtProcessor.generateToken(username, 1000L*60*60*5);

        log.info("생성된 토큰: {}", token);
        assertNotNull(token);
        assertTrue(token.contains("."));  // JWT 구조 확인 (Header.Payload.Signature)

        // 토큰 구조 검증
        String[] parts = token.split("\\.");
        log.info("Header   : {}", parts[0]);
        log.info("Payload  : {}", parts[1]);
        log.info("Signature: {}", parts[2]);
        assertEquals(3, parts.length, "JWT는 3부분으로 구성되어야 합니다.");
    }

    @DisplayName("사용자명 추출 테스트")
    @Test
    void getUsername() {
        Long memberId = 5L;
        String token = jwtProcessor.generateRefreshToken(memberId);

        // JWT Subject(username) 추출
        Long extracted = jwtProcessor.getMemberId(token);

        assertEquals(memberId, extracted);
    }



    @DisplayName("사용자명, 권한 추출 테스트")
    @Test
    void generateTokenWithRole() {
        long memberId = 3L;
        String role = "ROLE_ADMIN";
        String token = jwtProcessor.generateTokenWithRole(Long.toString(memberId), role);

        Long extractedId = jwtProcessor.getMemberId(token);
        String extractedRole = jwtProcessor.getRole(token);
        log.info("새로 생성한 토큰에서 추출한 : {}", extractedId);
        log.info("새로 생성한 토큰에서 추출한 권한: {}", extractedRole);

        // Then
        assertEquals(memberId, extractedId);
        assertEquals(role, extractedRole);
    }

    @DisplayName("사용자명, member_id, provider 추출 테스트")
    @Test
    void generateTokenWithId() {
        String email = "testUser@exampl.com";
        Long id = 1L;
//        String provider = "LOCAL";
        String token = jwtProcessor.generateAccessToken(id, email);

        String extractedUsername = jwtProcessor.getEmail(token);
        Long extractedId = jwtProcessor.getMemberId(token);
//        String extractedProvider = jwtProcessor.getProvider(token);
        log.info("새로 생성한 토큰에서 추출한 : {}", extractedUsername);
        log.info("새로 생성한 토큰에서 추출한 member_id: {}", extractedId);
//        log.info("새로 생성한 토큰에서 추출한 provider: {}", extractedProvider);

        // Then
        assertEquals(email, extractedUsername);
        assertEquals(id, extractedId);
//        assertEquals(provider, extractedProvider);
    }

    @DisplayName("새로 생성된 토큰 검증 테스트")
    @Test
    void validateToken_Valid() {
        // 새로 생성한 유효한 토큰
        String token = jwtProcessor.generateRefreshToken(1L);

        // JWT 검증 (유효 기간 및 서명 검증)
        boolean isValid = jwtProcessor.validateRefreshToken(token);

        assertTrue(isValid, "새로 생성한 토큰은 유효해야 합니다.");
    }

    /* 5분 경과 후 테스트 (수동으로 만료된 토큰 사용) */
    @Test
    void validateToken_Expired() {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMCIsImlhdCI6MTc1MDY3ODA2OCwiZXhwIjoxNzUwNjc4MzY4fQ.3a5iLHwO0dvg_QyaYn7ML5yn5kdsxh_uO88L_NQjjhU";


        assertThrows(ExpiredJwtException.class, () -> {
            jwtProcessor.getEmail(expiredToken);  // 만료된 토큰 사용 시 예외 발생
        });

        // 검증 메서드는 예외를 잡아서 false 반환
        boolean isValid = jwtProcessor.validateAccessToken(expiredToken);
        assertFalse(isValid, "만료된 토큰은 무효해야 합니다.");
    }

    @DisplayName("잘못된 형식의 토큰 검증 테스트")
    @Test
    void validateToken_Invalid() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtProcessor.validateAccessToken(invalidToken);

        assertFalse(isValid, "잘못된 형식의 토큰은 무효해야 합니다.");
    }

    /* 토큰 만료 시뮬레이션 */
    @Test
    void tokenExpiration_Simulation() throws InterruptedException {
        // Given - 매우 짧은 유효기간의 토큰 생성 (1초)
        // 실제로는 JwtProcessor에 테스트용 메서드 추가 필요
        String shortLivedToken = jwtProcessor.generateToken("testUser", 2000L);

        // When - 토큰 생성 직후에는 유효
        assertTrue(jwtProcessor.validateAccessToken(shortLivedToken));

        // 3초 대기
        Thread.sleep(3000);

        // Then - 만료 후에는 무효
        assertFalse(jwtProcessor.validateAccessToken(shortLivedToken));
    }
}
package com.banklab.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProcessor {

    // 테스트용 5분 - 만료 확인용 (수정 필요!!)
    static private final long TOKEN_VALID_MILLISECOND = 1000L * 60 * 5;

    private final JwtKeyManager jwtKeyManager;

    private Key getKey() {
        return jwtKeyManager.getKey();
    }

    /* ***** 토큰 생성 메서드 ***** */
    /**
     * JWT 토큰 생성
     * @param subject 사용자 식별자 (보통 username)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)                    // 사용자 식별자
                .setIssuedAt(new Date())               // 발급 시간
                .setExpiration(new Date(new Date().getTime() + TOKEN_VALID_MILLISECOND))  // 만료 시간
                .signWith(getKey())                     // 서명
                .compact();                            // 문자열 생성
    }

    /**
     * 권한 정보를 포함한 토큰 생성
     */
    public String generateTokenWithRole(String subject, String role) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + TOKEN_VALID_MILLISECOND))
                .claim("role", role)                   // 권한 정보 추가
                .signWith(getKey())
                .compact();
    }


    /**
     * 지정된 시간 후 만료되는 JWT 토큰 생성
     * @param subject 사용자 식별자 (보통 username)
     * @param tokenValidTime 만료 시간 (ms)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateTokenWithExpiry(String subject, Long tokenValidTime) {
        return Jwts.builder()
                .setSubject(subject)                    // 사용자 식별자
                .setIssuedAt(new Date())               // 발급 시간
                .setExpiration(new Date(new Date().getTime() + tokenValidTime))  // 만료 시간
                .signWith(getKey())                     // 서명
                .compact();                            // 문자열 생성
    }

    /* ***** 토큰 검증 및 정보 추출 ***** */

    /**
     * JWT Subject(username) 추출
     * @param token JWT 토큰
     * @return 사용자명
     * @throws JwtException 토큰 해석 불가 시 예외 발생
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * JWT Subject(role) 추출
     * @param token JWT 토큰
     * @return 사용자명
     * @throws JwtException 토큰 해석 불가 시 예외 발생
     */
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }


    /**
     * JWT 검증 (유효 기간 및 서명 검증)
     * @param token JWT 토큰
     * @return 검증 결과 (true: 유효, false: 무효)
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public long getRemainingExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

}


package com.banklab.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProcessor {
    static private final long ACCESS_TOKEN_VALID_MILLISECOND = 1000L * JwtConstants.ACCESS_TOKEN_EXP_SECONDS; // 15분
    static private final long REFRESH_TOKEN_VALID_MILLISECOND = 1000L * JwtConstants.REFRESH_TOKEN_EXP_SECONDS; // 7일

    private final JwtKeyManager jwtKeyManager;

    private Key getKey() {
        return jwtKeyManager.getKey();
    }

    public String generateAccessToken(Long memberId, String username, String provider) {
        return generateTokenWithClaims(String.valueOf(memberId), username, provider, ACCESS_TOKEN_VALID_MILLISECOND);
    }

    public String generateRefreshToken(Long memberId) {
        return generateToken(String.valueOf(memberId), REFRESH_TOKEN_VALID_MILLISECOND);
    }

    /* ***** 토큰 생성 메서드 ***** */
    /**
     * JWT 토큰 생성
     * @param subject 사용자 식별자 (member_id 사용)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String subject, Long tokenValidTime) {
        return Jwts.builder()
                .setSubject(subject)                    // 사용자 식별자
                .setIssuedAt(new Date())               // 발급 시간
                .setExpiration(new Date(new Date().getTime() + tokenValidTime))  // 만료 시간
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
                .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_VALID_MILLISECOND))
                .claim("role", role)                   // 권한 정보 추가
                .signWith(getKey())
                .compact();
    }

    /* ***** 토큰 생성 메서드 ***** */
    /**
     * JWT 토큰 생성
     * @param subject 사용자 식별자 (member_id)
     * @param email 사용자 로그인 ID(이메일)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateTokenWithClaims(String subject, String email, String provider, long tokenValidTime) {
        return Jwts.builder()
                .setSubject(subject)               // memberId
                .claim("email", email)
                .claim("provider", provider)   // 소셜/일반 구분
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(getKey())
                .compact();
    }

    /* ***** 토큰 검증 및 정보 추출 ***** */

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * JWT Subject(username) 추출
     * @param token JWT 토큰
     * @return 사용자명
     * @throws JwtException 토큰 해석 불가 시 예외 발생
     */
    public Long getMemberId(String token) {
        String id = getSubject(token);
        return Long.parseLong(id);
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
     * JWT Member ID 추출
     * @param token JWT 토큰
     * @return email
     * @throws JwtException 토큰 해석 불가 시 예외 발생
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);  // email claim 추출
    }

    public String getProvider(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("provider", String.class);
    }

    /**
     * Access 토큰 검증 (유효 기간 및 서명 검증)
     * @param token Access 토큰
     * @return 검증 결과 (true: 유효, false: 무효)
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Access Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token 추출: 쿠키 → 헤더 순서
     * @param request HttpServletRequest
     * @return Refresh Token 문자열 (없으면 null)
     */
    public String extractRefreshToken(HttpServletRequest request) {
        return CookieUtil.getCookieValue(request, "refreshToken");
    }

    public String extractAccessToken(HttpServletRequest request) {
        return CookieUtil.getCookieValue(request, "accessToken");
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


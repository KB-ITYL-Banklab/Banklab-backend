package com.banklab.account.util;

import com.banklab.account.mapper.AccountMapper;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 인증 관련 유틸리티 클래스
 * JWT 토큰에서 username을 추출하고, DB에서 memberId를 조회하는 기능 제공
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class AuthenticationUtils {

    private final JwtProcessor jwtProcessor;
    private final AccountMapper accountMapper;

    /**
     * HTTP 요청에서 JWT 토큰을 추출하고 검증한 후, username으로 memberId를 조회
     *
     * @param request HTTP 요청 객체
     * @return 로그인한 사용자의 memberId
     * @throws SecurityException 인증 실패 시
     */
    public Long getCurrentMemberId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                // JWT 토큰 검증
                if (!jwtProcessor.validateToken(token)) {
                    throw new SecurityException("유효하지 않은 토큰입니다.");
                }

                // 토큰에서 username 추출
                String username = jwtProcessor.getUsername(token);
                log.info("🔑 JWT에서 추출한 username: {}", username);

                // username으로 memberId 조회
                Long memberId = accountMapper.getMemberIdByUsername(username);
                log.info("🔍 DB 조회 결과 - username: {} → memberId: {}", username, memberId);

                if (memberId == null) {
                    log.error("❌ 사용자를 찾을 수 없습니다. username: {}", username);
                    throw new SecurityException("존재하지 않는 사용자입니다.");
                }

                log.info("✅ 인증 성공 - username: {} → memberId: {}", username, memberId);
                return memberId;

            } catch (Exception e) {
                log.error("토큰 처리 중 오류 발생: {}", e.getMessage());
                throw new SecurityException("인증 처리 중 오류가 발생했습니다.");
            }
        }
        throw new SecurityException("인증이 필요합니다.");
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출하고 username을 반환
     *
     * @param request HTTP 요청 객체
     * @return 로그인한 사용자의 username
     * @throws SecurityException 인증 실패 시
     */
    public String getCurrentUsername(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                // JWT 토큰 검증
                if (!jwtProcessor.validateToken(token)) {
                    throw new SecurityException("유효하지 않은 토큰입니다.");
                }

                return jwtProcessor.getUsername(token);

            } catch (Exception e) {
                log.error("토큰 처리 중 오류 발생: {}", e.getMessage());
                throw new SecurityException("인증 처리 중 오류가 발생했습니다.");
            }
        }
        throw new SecurityException("인증이 필요합니다.");
    }
}
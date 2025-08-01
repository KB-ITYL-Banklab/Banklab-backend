package com.banklab.security.handler;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.security.account.domain.CustomUser;
import com.banklab.security.account.dto.AuthResultDTO;
import com.banklab.security.account.dto.UserInfoDTO;
import com.banklab.security.util.JsonResponse;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    public static final int REFRESH_TOKEN_EXP_MINUTES = 60 * 24 * 7; // 7일
    public static final int REFRESH_TOKEN_EXP_SECONDS = REFRESH_TOKEN_EXP_MINUTES * 60;

    private final JwtProcessor jwtProcessor;
    private final RedisService redisService;

    // 인증 성공 결과 생성
    private AuthResultDTO makeAuthResult(HttpServletResponse response, CustomUser user) {
        String email = user.getUsername();
        Long memberId = user.getMember().getMemberId();

        // JWT 토큰 생성
        String access = jwtProcessor.generateAccessToken(memberId, email);
        String refresh = jwtProcessor.generateRefreshToken(memberId);

        // Redis에 저장 (key = "RT:<memberId>", value = token)
        redisService.set(RedisKeyUtil.refreshToken(memberId), refresh,  REFRESH_TOKEN_EXP_MINUTES ); // 7일 = 10080분

        response.addCookie(createRefreshTokenCookie(refresh));

        // access 토큰 + 사용자 기본 정보를 AuthResultDTO로 구성
        return new AuthResultDTO(access, UserInfoDTO.of(user.getMember()));
    }

    private Cookie createRefreshTokenCookie(String refresh) {
        // HttpOnly RefreshToken 쿠키 설정
        Cookie cookie = new Cookie("refreshToken", refresh);
        cookie.setHttpOnly(true); // JS 접근 불가
        cookie.setSecure(false);   // HTTPS 전용 (로컬 테스트는 false 허용으로 수정 필요!!!)
        cookie.setPath("/");      // 모든 경로에 전송
        cookie.setMaxAge(REFRESH_TOKEN_EXP_SECONDS); // 7일
//        cookie.setDomain("your-domain.com"); // 도메인 필요시 지정 (옵션)
        return cookie;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 인증 결과에서 사용자 정보 추출
        CustomUser user = (CustomUser) authentication.getPrincipal();

        // 인증 성공 결과를 JSON으로 직접 응답
        AuthResultDTO result = makeAuthResult(response, user);
        JsonResponse.send(response, result);
    }
}

package com.banklab.security.service;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.util.CookieUtil;
import com.banklab.security.util.JwtConstants;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    public static final int REFRESH_TOKEN_EXP_MINUTES = JwtConstants.REFRESH_TOKEN_EXP_SECONDS / 60;

    private final JwtProcessor jwtProcessor;
    private final RedisService redisService;

    public void issueTokenAndSetCookie(HttpServletResponse response, MemberVO member) {
        String email = member.getEmail();
        Long memberId = member.getMemberId();
        String provider = member.getProvider().name();

        // JWT 토큰 생성
        String access = jwtProcessor.generateAccessToken(memberId, email, provider);
        String refresh = jwtProcessor.generateRefreshToken(memberId);

        // Redis에 저장 (key = "RT:<memberId>", value = token)
        redisService.set(RedisKeyUtil.refreshToken(memberId), refresh,  REFRESH_TOKEN_EXP_MINUTES);

        CookieUtil.addCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, access, JwtConstants.ACCESS_TOKEN_EXP_SECONDS);
        CookieUtil.addCookie(response, JwtConstants.REFRESH_TOKEN_COOKIE_NAME, refresh, JwtConstants.REFRESH_TOKEN_EXP_SECONDS);
    }
}

package com.banklab.security.util;

public final class JwtConstants {
    private JwtConstants() {} // 인스턴스화 방지

    // 단위: 초 (쿠키 저장 등)
    public static final int ACCESS_TOKEN_EXP_SECONDS = 60 * 15; // 15분
    public static final int REFRESH_TOKEN_EXP_SECONDS = 60 * 60 * 24 * 7; // 7일
    // 쿠키 name
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
}

package com.banklab.security.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class CookieUtil {

    // 쿠키 생성 및 응답에 추가
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);             // 초 단위: 60 * 60 * 24 = 1일
        cookie.setHttpOnly(true);             // JS 접근 불가
        cookie.setSecure(false);             // HTTPS 전용X (로컬 테스트는 false 허용으로 수정 필요!!!)
        cookie.setPath("/");                 // 모든 경로에 전송

        response.addCookie(cookie);
    }

    // 쿠키 삭제 (만료시간 0)
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);                  // 즉시 만료
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // 쿠키 값 조회
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}


package com.banklab.typetest.util;

import javax.servlet.http.HttpServletRequest;

public class JwtTokenUtil {
    /**
     * Authorization 헤더에서 Bearer 토큰만 추출
     */
    public static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}


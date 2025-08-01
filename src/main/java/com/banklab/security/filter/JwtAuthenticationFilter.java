package com.banklab.security.filter;

import com.banklab.common.redis.RedisService;
import com.banklab.security.util.JsonResponse;
import com.banklab.security.util.JwtProcessor;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProcessor jwtProcessor;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;

    private Authentication getAuthentication(String accessToken) {
        String username = jwtProcessor.getEmail(accessToken);
        UserDetails principal = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtProcessor.extractAccessToken(request);

        if (StringUtils.hasText(accessToken)) {
            // 로그아웃(차단)된 토큰인지 Redis에서 검사
            if (redisService.isBlacklisted(accessToken)) {
                JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "BLACKLISTED_TOKEN");
                return;
            }

            try {
                // 토큰에서 사용자 정보 추출 및 Authentication 객체 구성 후 SecurityContext에 저장
                Authentication authentication = getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                log.warn("Access Token 만료: {}", e.getMessage());
                JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED");
                return;
            } catch (Exception e) {
                log.error("JWT 인증 실패: {}", e.getMessage());
                JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN");
                return;
            }
        }
        super.doFilter(request, response, filterChain);
    }
}

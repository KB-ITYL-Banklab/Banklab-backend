package com.banklab.security.controller;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.member.service.MemberService;
import com.banklab.security.account.dto.AuthResultDTO;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RedisService redisService;
    private final JwtProcessor jwtProcessor;
    private final MemberService memberService;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Access Token blacklist 등록
        String token = jwtProcessor.extractAccessToken(request);
        if (StringUtils.hasText(token) && jwtProcessor.validateAccessToken(token)) {
            long remaining = jwtProcessor.getRemainingExpiration(token);
            redisService.blacklistToken(token, remaining);
        }

        // refresh 토큰 삭제
        String refreshToken = jwtProcessor.extractRefreshToken(request);
        if (StringUtils.hasText(refreshToken) && jwtProcessor.validateRefreshToken(refreshToken)) {
            Long memberId = jwtProcessor.getMemberId(refreshToken);
            redisService.delete(RedisKeyUtil.refreshToken(memberId));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResultDTO> reissue(HttpServletRequest request) {
        String refreshToken = jwtProcessor.extractRefreshToken(request); // 쿠키에서 추출

        if (!StringUtils.hasText(refreshToken) || !jwtProcessor.validateRefreshToken(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 Refresh Token입니다");
        }

        Long memberId = jwtProcessor.getMemberId(refreshToken);
        String redisKey = RedisKeyUtil.refreshToken(memberId);
        String savedRefreshToken = redisService.get(redisKey);

        // 탈취 감지: 저장된 토큰과 일치하지 않으면 로그아웃 처리
        if (!refreshToken.equals(savedRefreshToken)) {
            redisService.delete(redisKey); // 탈취된 토큰 무효화
            throw new IllegalStateException("Refresh Token 정보가 일치하지 않습니다");
        }

        String email = memberService.findEmailByMemberId(memberId);
        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException("존재하지 않는 사용자입니다");
        }
        String newAccessToken = jwtProcessor.generateAccessToken(memberId, email);

        return ResponseEntity.ok(new AuthResultDTO(newAccessToken, null));
    }
}

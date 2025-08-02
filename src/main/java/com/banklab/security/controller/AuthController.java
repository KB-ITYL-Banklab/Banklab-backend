package com.banklab.security.controller;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.member.dto.MemberDTO;
import com.banklab.member.service.MemberService;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.account.dto.UserInfoDTO;
import com.banklab.security.oauth2.domain.OAuthProvider;
import com.banklab.security.service.LoginUserProvider;
import com.banklab.security.util.CookieUtil;
import com.banklab.security.util.JwtConstants;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RedisService redisService;
    private final JwtProcessor jwtProcessor;
    private final MemberService memberService;
    private final LoginUserProvider loginUserProvider;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Access Token blacklist 등록
        String token = jwtProcessor.extractAccessToken(request);
        if (StringUtils.hasText(token) && jwtProcessor.validateAccessToken(token)) {
            long remaining = jwtProcessor.getRemainingExpiration(token);
            redisService.blacklistToken(token, remaining);
            CookieUtil.deleteCookie(response, "accessToken");
        }

        // refresh 토큰 삭제
        String refreshToken = jwtProcessor.extractRefreshToken(request);
        if (StringUtils.hasText(refreshToken) && jwtProcessor.validateRefreshToken(refreshToken)) {
            Long memberId = jwtProcessor.getMemberId(refreshToken);
            redisService.delete(RedisKeyUtil.refreshToken(memberId));
            CookieUtil.deleteCookie(response, "refreshToken");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getMe() {
        MemberVO member = loginUserProvider.getLoginUser();
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UserInfoDTO.of(member));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
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

        MemberDTO member = memberService.get(memberId, null);
        String email = member.getEmail();
        OAuthProvider provider = member.getProvider();
        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException("존재하지 않는 사용자입니다");
        }
        String newAccessToken = jwtProcessor.generateAccessToken(memberId, email, provider.name());
        CookieUtil.addCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, newAccessToken, JwtConstants.ACCESS_TOKEN_EXP_SECONDS);

        return ResponseEntity.ok().build();
    }
}

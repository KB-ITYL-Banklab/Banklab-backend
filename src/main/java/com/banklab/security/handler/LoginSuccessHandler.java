package com.banklab.security.handler;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProcessor jwtProcessor;

    private AuthResultDTO makeAuthResult(CustomUser user) {
        String email = user.getUsername();
        Long memberId = user.getMember().getMemberId();

        // JWT 토큰 생성
        String token = jwtProcessor.generateTokenWithId(email, memberId);

        // 토큰 + 사용자 기본 정보를 AuthResultDTO로 구성
        return new AuthResultDTO(token, UserInfoDTO.of(user.getMember()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 인증 결과에서 사용자 정보 추출
        CustomUser user = (CustomUser) authentication.getPrincipal();

        // 인증 성공 결과를 JSON으로 직접 응답
        AuthResultDTO result = makeAuthResult(user);
        JsonResponse.send(response, result);
    }
}

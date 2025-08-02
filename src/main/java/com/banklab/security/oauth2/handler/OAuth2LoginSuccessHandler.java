package com.banklab.security.oauth2.handler;

import com.banklab.security.oauth2.domain.CustomOAuth2User;
import com.banklab.security.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthTokenService authTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 인증 결과에서 사용자 정보 추출
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        authTokenService.issueTokenAndSetCookie(response, user.getMember());
        // 프론트엔드로 리디렉션, 토큰을 쿼리 파라미터로 포함
        String redirectUrl = "http://localhost:5173/oauth2/callback";
        response.sendRedirect(redirectUrl);
    }
}


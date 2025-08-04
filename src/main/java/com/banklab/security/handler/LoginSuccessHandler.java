package com.banklab.security.handler;

import com.banklab.security.account.domain.CustomUser;
import com.banklab.security.account.dto.UserInfoDTO;
import com.banklab.security.service.AuthTokenService;
import com.banklab.security.util.JsonResponse;
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
    private final AuthTokenService authTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 인증 결과에서 사용자 정보 추출
        CustomUser user = (CustomUser) authentication.getPrincipal();

        authTokenService.issueTokenAndSetCookie(response, user.getMember());
        JsonResponse.send(response, UserInfoDTO.of(user.getMember()));
    }
}

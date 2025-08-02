package com.banklab.security.oauth2.handler;

import com.banklab.security.util.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 예외 메시지를 로그에 출력하여 디버깅
        String errorMessage = exception.getMessage();
        log.info("OAuth2 로그인 실패: " + errorMessage);  // 로그에 출력 (실제 사용 시 log4j나 slf4j 사용)

        // UNAUTHORIZED 상태와 에러 메시지로 응답
        JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패하였습니다.");
    }
}

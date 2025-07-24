package com.banklab.security.service;

//import com.banklab.security.dto.LoginRequest;
//import com.banklab.security.dto.LoginResponse;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
//
//    private final JwtProcessor jwtProcessor;
//
//    public LoginResponse login(LoginRequest loginRequest) {
//        // 실제 인증 로직 구현 (예: 사용자 이름/비밀번호 확인, DB 조회)
//        // 여기서는 임시로 성공적인 로그인 응답을 반환합니다.
//        // 실제 애플리케이션에서는 사용자 인증 후 JWT 토큰을 생성해야 합니다.
//
//        String accessToken = jwtProcessor.createAccessToken(loginRequest.getUsername());
//        String refreshToken = jwtProcessor.createRefreshToken(loginRequest.getUsername());
//
//        return new LoginResponse(accessToken, refreshToken, "로그인 성공");
//    }
}

package com.banklab.oauth.controller;

import com.banklab.oauth.service.KakaoAuthService;
import com.banklab.security.account.dto.AuthResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoOAuthService;

    @GetMapping("/login") //post vs get 고민해볼것!! requestBody로 변경 (code가 유출되지 않으려면 post)
    public ResponseEntity<?> login(@RequestParam String code) {
        AuthResultDTO result = kakaoOAuthService.login(code);
        return ResponseEntity.ok(result);
    }
}

package com.banklab.security.controller;

import com.banklab.common.redis.RedisService;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RedisService redisService;
    private final JwtProcessor tokenProcessor;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        long remaining = tokenProcessor.getRemainingExpiration(token);

        redisService.blacklistToken(token, remaining);
        return ResponseEntity.ok().build();
    }
}

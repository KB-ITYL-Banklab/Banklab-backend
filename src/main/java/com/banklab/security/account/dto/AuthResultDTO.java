package com.banklab.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResultDTO {
    private String accessToken;   // JWT access 토큰
    private UserInfoDTO user;    // 사용자 기본 정보
}

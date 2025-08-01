package com.banklab.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {
    private String email;
    private String newPassword;
    private Boolean emailVerified; // true → 이메일 인증, false → 전화번호 인증
}

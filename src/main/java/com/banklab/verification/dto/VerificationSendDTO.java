package com.banklab.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSendDTO {
    private String target;                 // 이메일 또는 전화번호
    private Boolean isEmail;               // EMAIL or PHONE
    private Boolean isSignup;             // true: 회원가입, false: 비밀번호 재설정
}

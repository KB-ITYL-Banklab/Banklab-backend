package com.banklab.verification.sender;

public interface VerificationSender {
    void sendCode(String target);                  // 인증번호 발송
    boolean verifyCode(String target, String code); // 인증번호 검증
}

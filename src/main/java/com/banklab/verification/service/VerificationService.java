package com.banklab.verification.service;

import com.banklab.verification.dto.VerificationVerifyDTO;

public interface VerificationService {
    /**
     * 인증코드 전송
     * @param target
     * @param isEmail
     */
    void sendCode(String target, boolean isEmail);

    /**
     * 인증코드 검증
     * @param dto
     * @return
     */
    boolean verifyCode(VerificationVerifyDTO dto);

    /**
     * 이메일을 통해 정보 알아낸 후 전화번호 또는 이메일로 인증코드 전송
     * @param email
     * @param isEmail
     */
    void sendCodeByEmailOrPhone(String email, boolean isEmail);
}

package com.banklab.verification.service;

import com.banklab.verification.dto.VerificationVerifyDTO;

public interface VerificationService {
    void sendCode(String target, boolean isEmail);
    boolean verifyCode(VerificationVerifyDTO dto);
    void sendCodeByEmailOrPhone(String email, boolean isEmail);
}

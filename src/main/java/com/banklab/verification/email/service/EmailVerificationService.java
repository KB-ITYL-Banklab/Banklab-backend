package com.banklab.verification.email.service;

import com.banklab.verification.email.dto.EmailVerifyDTO;

public interface EmailVerificationService {
    void sendVerificationCode(String toEmail);
    boolean verifyCode(EmailVerifyDTO dto);
}

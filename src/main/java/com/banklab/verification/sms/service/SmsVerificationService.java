package com.banklab.verification.sms.service;

public interface SmsVerificationService {
    void sendVerificationCode(String phone);
    boolean verifyCode(String phone, String inputCode);
}

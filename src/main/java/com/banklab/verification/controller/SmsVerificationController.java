package com.banklab.verification.controller;

import com.banklab.verification.dto.PhoneSendDTO;
import com.banklab.verification.dto.PhoneVerifyDTO;
import com.banklab.verification.service.SmsVerificationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification/sms")
public class SmsVerificationController {

    private final SmsVerificationService smsService;

    @PostMapping("/send")
    @ApiOperation(value = "인증번호 전송")
    public ResponseEntity<Object> sendCode(@RequestBody PhoneSendDTO request){
        String phoneNum = request.getPhone().replace("-", "");
        smsService.sendVerificationCode(phoneNum);
        return ResponseEntity.ok("인증번호 발송 완료");
    }

    @PostMapping("/verify")
    @ApiOperation(value = "인증번호 검증")
    public ResponseEntity<String> verifyCode(@RequestBody PhoneVerifyDTO request){
        String phoneNum = request.getPhone().replace("-", "");
        boolean success = smsService.verifyCode(phoneNum, request.getCode());
        return ResponseEntity.ok(success ? "인증 성공" : "인증 실패");
    }

}
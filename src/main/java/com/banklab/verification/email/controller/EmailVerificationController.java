package com.banklab.verification.email.controller;

import com.banklab.verification.email.dto.EmailSendDTO;
import com.banklab.verification.email.dto.EmailVerifyDTO;
import com.banklab.verification.email.service.EmailVerificationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification/email")
public class EmailVerificationController {

    private final EmailVerificationService emailService;

    @PostMapping("/send")
    @ApiOperation(value = "인증번호 전송")
    public ResponseEntity<String> sendCode(@RequestBody EmailSendDTO request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("이메일 전송 완료");
    }

    @PostMapping("/verify")
    @ApiOperation(value = "인증번호 검증")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyDTO request){
        boolean success = emailService.verifyCode(request);
        return ResponseEntity.ok(success ? "인증 성공" : "인증 실패");
    }
}

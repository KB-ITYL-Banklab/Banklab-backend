package com.banklab.verification.controller;

import com.banklab.common.response.StatusResponse;
import com.banklab.member.service.MemberService;
import com.banklab.verification.dto.VerificationSendDTO;
import com.banklab.verification.dto.VerificationVerifyDTO;
import com.banklab.verification.service.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Api(tags = "인증 API", description = "인증번호 전송, 검증")
public class VerificationController {
    private final VerificationService verificationService;
    private final MemberService memberService;

    @PostMapping("/send")
    @ApiOperation(value = "인증번호 전송")
    public ResponseEntity<StatusResponse> sendCode(@RequestBody VerificationSendDTO request) {
        String target = request.getTarget();
        boolean isEmail = request.getIsEmail();

        // 이메일 인증일 경우 가입 여부 검사
        if (isEmail) {
            if (request.getIsSignup()) {
                if (memberService.existsByEmail(target)) {
                    return ResponseEntity.status(409).body(new StatusResponse(false, "이미 가입된 이메일입니다."));
                }
            } else {
                if (!memberService.existsByEmail(target)) {
                    return ResponseEntity.status(404).body(new StatusResponse(false, "가입된 이메일이 없습니다."));
                }
            }
        }

        verificationService.sendCode(target, isEmail);
        return ResponseEntity.ok(new StatusResponse(true, "인증번호 전송 완료"));
    }

    @PostMapping("/verify")
    @ApiOperation(value = "인증번호 검증")
    public ResponseEntity<StatusResponse> verifyCode(@RequestBody VerificationVerifyDTO request) {
        boolean success = verificationService.verifyCode(request);
        return ResponseEntity.ok(new StatusResponse(success, success ? "인증 성공" : "인증 실패"));
    }

    @PostMapping("/code/reset-password")
    @ApiOperation(value = "비밀번호 재설정을 위한 인증번호 전송 (이메일 or 전화번호)")
    public ResponseEntity<StatusResponse> sendCodeByEmail(@RequestBody VerificationSendDTO request) {
        String email = request.getTarget();
        boolean isEmail = request.getIsEmail();

        verificationService.sendCodeByEmailOrPhone(email, isEmail);
        return ResponseEntity.ok(new StatusResponse(true, "인증번호 전송 완료"));
    }

    @PostMapping("/verify/reset-password")
    public ResponseEntity<StatusResponse> verifyCodeByEmail(@RequestBody VerificationVerifyDTO request) {
        if (!request.getIsEmail()) {
            String phone = memberService.getPhoneByEmail(request.getTarget());
            request.setTarget(phone);
        }
        boolean success = verificationService.verifyCode(request);
        return ResponseEntity.ok(new StatusResponse(success, success ? "인증 성공" : "인증 실패"));
    }
}

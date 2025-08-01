package com.banklab.member.controller;

import com.banklab.member.dto.*;
import com.banklab.member.service.MemberService;
import com.banklab.security.service.LoginUserProvider;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService service;
    private final LoginUserProvider loginUserProvider;

    @PostMapping("")
    @ApiOperation(value = "회원가입")
    public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
        return ResponseEntity.ok(service.join(member));
    }

    // ID(email) 중복 체크 API
    @GetMapping("/exist/email/{email}")
    @ApiOperation(value = "ID(이메일) 존재 여부 확인", notes = "회원가입, 비밀번호 찾기에서 사용")
    public ResponseEntity<Boolean> checkEmailExist(@PathVariable String email) {
        return ResponseEntity.ok().body(service.existsByEmail(email));
    }

    @PutMapping("")
    @ApiOperation(value = "회원 정보 업데이트")
    public ResponseEntity<MemberDTO> update(@RequestBody MemberUpdateDTO request) {
        Long memberId = loginUserProvider.getLoginMemberId(); // 로그인된 사용자 ID
        return ResponseEntity.ok(service.update(memberId, request));
    }

    // 비밀번호 재설정
    @PostMapping("/password/reset")
    @ApiOperation(value = "비밀번호 찾기(재설정)", notes = "가입된 아이디와 인증 기반으로 비밀번호 재설정")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO dto) {
        service.resetPassword(dto);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    @PostMapping("/find/email")
    @ApiOperation(value = "ID(이메일) 찾기", notes = "개인 정보와 인증 기반으로 아이디 찾기")
    public ResponseEntity<FindResponseDTO> findUsername(@RequestBody PersonalInfoDTO request) {
        return ResponseEntity.ok().body(service.findEmail(request));
    }
}

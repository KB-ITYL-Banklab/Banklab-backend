package com.banklab.member.controller;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberJoinDTO;
import com.banklab.member.dto.MemberUpdateDTO;
import com.banklab.member.service.MemberService;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService service;
    private final PasswordEncoder passwordEncoder;  // 비밀번호 암호화
    private final JwtProcessor jwtProcessor;

    // 회원가입 API
    @PostMapping("")
    public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
        return ResponseEntity.ok(service.join(member.toVO(passwordEncoder)));
    }

    // ID(email) 중복 체크 API
    @GetMapping("/checkusername/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        return ResponseEntity.ok().body(service.checkDuplicate(username));
    }

    @PutMapping("")
    public ResponseEntity<MemberDTO> update(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody MemberUpdateDTO request) {
        // JWT 토큰에서 사용자 ID 추출
        String token = authorizationHeader.replace("Bearer ", "");
        Long memberId = jwtProcessor.getMemberId(token);  // JWT에서 memberId 추출

        // 사용자 정보 수정
        return ResponseEntity.ok(service.update(memberId, request));
    }
}

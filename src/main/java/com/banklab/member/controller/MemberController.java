package com.banklab.member.controller;

import com.banklab.member.dto.*;
import com.banklab.member.service.MemberService;
import com.banklab.security.util.JwtProcessor;
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

    private final JwtProcessor jwtProcessor;

    @PostMapping("")
    @ApiOperation(value = "회원가입")
    public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
        return ResponseEntity.ok(service.join(member));
    }

    // ID(email) 중복 체크 API
    @GetMapping("/checkusername/{username}")
    @ApiOperation(value = "ID(이메일) 중복 체크")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        return ResponseEntity.ok().body(service.checkDuplicate(username));
    }

    @PutMapping("")
    @ApiOperation(value = "회원 정보 업데이트")
    public ResponseEntity<MemberDTO> update(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody MemberUpdateDTO request) {
        // JWT 토큰에서 사용자 ID 추출
        String token = authorizationHeader.replace("Bearer ", "");
        Long memberId = jwtProcessor.getMemberId(token);  // JWT에서 memberId 추출

        // 사용자 정보 수정
        return ResponseEntity.ok(service.update(memberId, request));
    }

    @PostMapping("/find/username")
    @ApiOperation(value = "ID(이메일) 찾기", notes = "개인 정보와 인증 기반으로 아이디 찾기")
    public ResponseEntity<FindResponseDTO> findUsername(@RequestBody PersonalInfoDTO request) {
        return ResponseEntity.ok().body(service.findEmail(request));
    }
}

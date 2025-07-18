package com.banklab.member.controller;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberJoinDTO;
import com.banklab.member.service.MemberService;
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

    // 회원가입 API
    @PostMapping("")
    public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
        return ResponseEntity.ok(service.join(member));
    }
}

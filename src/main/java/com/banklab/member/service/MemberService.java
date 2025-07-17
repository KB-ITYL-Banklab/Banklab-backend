package com.banklab.member.service;

import com.banklab.member.dto.MemberDTO;

public interface MemberService {
    MemberDTO get(String username);              // 회원 조회
}

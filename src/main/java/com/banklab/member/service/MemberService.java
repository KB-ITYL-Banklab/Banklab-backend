package com.banklab.member.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberJoinDTO;

public interface MemberService {
    MemberDTO get(String username);              // 회원 조회
    MemberDTO join(MemberJoinDTO member);        // 회원가입
    boolean checkDuplicate(String username);    // ID 중복 체크
}

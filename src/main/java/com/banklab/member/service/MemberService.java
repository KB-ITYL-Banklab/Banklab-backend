package com.banklab.member.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberJoinDTO;
import com.banklab.security.account.domain.MemberVO;

public interface MemberService {
    MemberDTO get(String email);                // 회원 조회
    MemberDTO join(MemberVO member);        // 회원가입
    boolean checkDuplicate(String email);    // ID 중복 체크
}

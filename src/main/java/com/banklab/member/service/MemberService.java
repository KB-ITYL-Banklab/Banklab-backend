package com.banklab.member.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberUpdateDTO;
import com.banklab.security.account.domain.MemberVO;

public interface MemberService {
    MemberDTO get(Long id, String email);    // 회원 조회 (두 파라미터 중 하나는 null이어도 됨)
    MemberDTO join(MemberVO member);        // 회원가입
    boolean checkDuplicate(String email);    // ID 중복 체크
    MemberDTO update(Long id, MemberUpdateDTO member);
}

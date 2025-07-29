package com.banklab.member.service;

import com.banklab.member.dto.*;
import com.banklab.security.account.domain.MemberVO;

public interface MemberService {
    MemberDTO get(Long id, String email);    // 회원 조회 (두 파라미터 중 하나는 null이어도 됨)
    MemberDTO join(MemberJoinDTO member);        // 회원가입
    MemberDTO registerMember(MemberVO member);
    boolean checkDuplicate(String email);    // ID 중복 체크
    MemberDTO update(Long id, MemberUpdateDTO member);
    FindResponseDTO findEmail(PersonalInfoDTO dto);
}

package com.banklab.member.mapper;

import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;

public interface MemberMapper {
    MemberVO get(String username);
    int insert(MemberVO member);                      // 회원정보 저장
    int insertAuth(AuthVO auth);                      // 권한정보 저장
}

package com.banklab.member.mapper;

import com.banklab.security.account.domain.MemberVO;

public interface MemberMapper {
    MemberVO get(String username);
}

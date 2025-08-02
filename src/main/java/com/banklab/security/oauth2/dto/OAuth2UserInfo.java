package com.banklab.security.oauth2.dto;

import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public abstract MemberVO toVO();
}

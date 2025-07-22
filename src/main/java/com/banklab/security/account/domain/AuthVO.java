package com.banklab.security.account.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthVO implements GrantedAuthority {
    private Long memberId;
    private String auth;

    @Override
    public String getAuthority() {
        return auth;
    }
}

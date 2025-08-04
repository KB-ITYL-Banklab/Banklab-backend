package com.banklab.security.util;

import com.banklab.security.account.domain.CustomUser;
import com.banklab.security.account.domain.MemberVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoginUserProvider {
    public MemberVO getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUser) {
            return ((CustomUser) principal).getMember();
        }

        return null;
    }

    public Long getLoginMemberId() {
        MemberVO user = getLoginUser();
        return user != null ? user.getMemberId() : null;
    }

    public String getLoginEmail() {
        MemberVO user = getLoginUser();
        return user != null ? user.getEmail() : null;
    }
}

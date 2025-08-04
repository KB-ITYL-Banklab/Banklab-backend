package com.banklab.security.oauth2.domain;

import com.banklab.security.account.domain.MemberVO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private MemberVO member;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }

    public CustomOAuth2User(Map<String, Object> attributes,
                            String nameAttributeKey,
                            MemberVO member) {
        super(member.getAuthList(), attributes, nameAttributeKey);
        this.member = member;
    }
}

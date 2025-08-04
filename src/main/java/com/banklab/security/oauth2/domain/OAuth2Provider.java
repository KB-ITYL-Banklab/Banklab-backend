package com.banklab.security.oauth2.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum OAuth2Provider {
    KAKAO("kakao", "kakao_account", "id"),
    NAVER("naver", "response", "id"),
    LOCAL("local", "local", "email");

    private final String registrationId;    // Spring Security registrationId (필수)
    private final String attributeKey;  // 응답 JSON의 루트 키
    private final String idAttribute;       // 사용자 식별 키 (id)

    public static OAuth2Provider from(String registrationId) {
        return Arrays.stream(OAuth2Provider.values())
                .filter(p -> p.registrationId.equalsIgnoreCase(registrationId))
                .findFirst()
                .orElse(LOCAL);
    }
}

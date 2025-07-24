package com.banklab.oauth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum OAuthProvider {
    KAKAO("kakao_account", "id", "email"),
    NAVER("response", "id", "email"),
    LOCAL("local", "local", "email");

    private final String attributeKey;
    private final String providerCode;
    private final String identifier;

    public static OAuthProvider from(String provider) {
        return Arrays.stream(OAuthProvider.values())
                .filter(p -> p.getProviderCode().equalsIgnoreCase(provider))
                .findFirst()
                .orElse(LOCAL);
    }
}

package com.banklab.security.oauth2.dto;

import com.banklab.security.oauth2.domain.OAuth2Provider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributesDTO {
    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값
    private OAuth2UserInfo oAuth2UserInfo; // 해당 소셜 로그인 유저 정보

    public static OAuthAttributesDTO of(OAuth2Provider provider, Map<String, Object> attributes) {
        if (provider.equals(OAuth2Provider.KAKAO)) {
            return ofKakao(attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider: " + provider);
    }

    public static OAuthAttributesDTO ofKakao(Map<String, Object> attributes) {
        return OAuthAttributesDTO.builder()
                .nameAttributeKey(OAuth2Provider.KAKAO.getIdAttribute())
                .oAuth2UserInfo(new KakaoUserInfoDTO(attributes))
                .build();
    }
}

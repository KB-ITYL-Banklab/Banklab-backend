package com.banklab.security.oauth2.dto;

import com.banklab.security.oauth2.domain.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributesDTO {
    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값
    private OAuth2UserInfo oAuth2UserInfo; // 해당 소셜 로그인 유저 정보

    public static OAuthAttributesDTO of(OAuthProvider provider,
                                        String nameAttributeKey, Map<String, Object> attributes) {
        if (provider.equals(OAuthProvider.KAKAO)) {
            return ofKakao(nameAttributeKey, attributes);
        }
        return null;
    }

    public static OAuthAttributesDTO ofKakao(String nameAttributeKey, Map<String, Object> attributes) {
        return OAuthAttributesDTO.builder()
                .nameAttributeKey(nameAttributeKey)
                .oAuth2UserInfo(new KakaoUserInfoDTO(attributes))
                .build();
    }
}

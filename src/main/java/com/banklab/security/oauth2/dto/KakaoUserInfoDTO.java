package com.banklab.security.oauth2.dto;

import com.banklab.member.domain.Gender;
import com.banklab.security.oauth2.domain.OAuth2Provider;
import com.banklab.security.account.domain.MemberVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Slf4j
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoDTO extends OAuth2UserInfo {
    private Long id;
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String name;
        private String email;
        private String birthyear;
        private String birthday;
        private String gender;
        private String phoneNumber;
    }

    public KakaoUserInfoDTO(Map<String, Object> attributes) {
        this.id = Long.valueOf(attributes.get(OAuth2Provider.KAKAO.getIdAttribute()).toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> account = (Map<String, Object>) attributes.get(OAuth2Provider.KAKAO.getAttributeKey());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.kakaoAccount = mapper.convertValue(account, KakaoAccount.class);
    }

    public MemberVO toVO() {
        return MemberVO.builder()
                .email(kakaoAccount.email)
                .name(kakaoAccount.name)
                .phone(kakaoAccount.phoneNumber.replaceAll("^\\+82\\s?", "0").replaceAll("[-\\s]", ""))
                .gender(Gender.fromString(kakaoAccount.gender))
                .birth(LocalDate.parse(kakaoAccount.birthyear + kakaoAccount.birthday, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .provider(OAuth2Provider.KAKAO)
                .providerId(id)
                .build();
    }
}

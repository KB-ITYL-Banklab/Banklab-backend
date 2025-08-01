package com.banklab.oauth.dto;

import com.banklab.member.domain.Gender;
import com.banklab.oauth.domain.OAuthProvider;
import com.banklab.security.account.domain.MemberVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoDTO {
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

    public MemberVO toVO() {
        return MemberVO.builder()
                .email(kakaoAccount.email)
                .name(kakaoAccount.name)
                .phone(kakaoAccount.phoneNumber)
                .gender(Gender.fromString(kakaoAccount.gender))
                .birth(LocalDate.parse(kakaoAccount.birthyear + kakaoAccount.birthday, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .provider(OAuthProvider.KAKAO)
                .providerId(id)
                .build();
    }
}

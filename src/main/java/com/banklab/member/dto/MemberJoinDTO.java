package com.banklab.member.dto;

import com.banklab.member.domain.Gender;
import com.banklab.oauth.domain.OAuthProvider;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinDTO {
    private String email;      // 로그인 ID
    private String password;   // 평문 비밀번호
    private String name;
    private String phone;
    private Gender gender;     // enum: MALE / FEMALE
    private String birth;      // 예: "2009-10-16"

    public MemberVO toVO(PasswordEncoder encoder) {
        return MemberVO.builder()
                .email(email)
                .password(encoder.encode(password))
                .name(name)
                .phone(phone)
                .gender(gender)
                .birth(LocalDate.parse(birth))
                .provider(OAuthProvider.LOCAL)
                .build();
    }
}

package com.banklab.member.dto;

import com.banklab.member.domain.Gender;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinDTO {
    private String username;   // 로그인 ID
    private String password;   // 평문 비밀번호
    private String name;
    private String email;
    private String phone;
    private Gender gender;     // enum: MALE / FEMALE
    private String birth;   // 예: "2009-10-16"

    public MemberVO toVO(PasswordEncoder encoder) {
        return MemberVO.builder()
                .username(username)
                .password(encoder.encode(password))
                .name(name)
                .email(email)
                .phone(phone)
                .gender(gender)
                .birth(LocalDate.parse(birth))
                .build();
    }
}

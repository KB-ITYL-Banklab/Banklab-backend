package com.banklab.member.dto;

import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDTO {
    private String email;
    private String password;
    private String name;
    private String phone;

    public MemberVO toVO(Long id) {
        return MemberVO.builder()
                .memberId(id)
                .email(email)
                .name(name)
                .phone(phone)
                .build();
    }
}

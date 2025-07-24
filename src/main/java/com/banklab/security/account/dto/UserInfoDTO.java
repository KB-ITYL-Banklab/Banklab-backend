package com.banklab.security.account.dto;

import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String name;
    private String email;
    private List<String> roles;

    public static UserInfoDTO of(MemberVO member) {
        return new UserInfoDTO(
                member.getName(),
                member.getEmail(),
                member.getAuthList().stream()
                        .map(AuthVO::getAuth)
                        .toList()
        );
    }
}


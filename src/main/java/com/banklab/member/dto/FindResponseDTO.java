package com.banklab.member.dto;

import com.banklab.oauth.domain.OAuthProvider;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindResponseDTO {
    private String email;
    private Date regDate;
    private OAuthProvider provider;

    public static FindResponseDTO of(MemberVO m) {
        return FindResponseDTO.builder()
                .email(m.getEmail())
                .regDate(m.getRegDate())
                .provider(m.getProvider())
                .build();
    }
}

package com.banklab.member.dto;

import com.banklab.security.oauth2.domain.OAuthProvider;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindResponseDTO {
    private String email;
    private String regDate;
    private OAuthProvider provider;

    public static FindResponseDTO of(MemberVO m) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return FindResponseDTO.builder()
                .email(m.getEmail())
                .regDate(sdf.format(m.getRegDate()))
                .provider(m.getProvider())
                .build();
    }
}

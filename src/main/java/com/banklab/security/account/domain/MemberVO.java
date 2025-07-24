package com.banklab.security.account.domain;

import com.banklab.member.domain.Gender;
import com.banklab.oauth.domain.OAuthProvider;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberVO {
    private Long memberId;
    private String email;
    private String password;
    private String name;
    private String phone;
    private Gender gender;
    private LocalDate birth;
    private OAuthProvider provider;
    private Long providerId;
    private Date regDate;
    private Date updateDate;

    @Builder.Default
    private List<AuthVO> authList = new ArrayList<>();
}

package com.banklab.security.account.domain;

import com.banklab.member.domain.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberVO {
    private Long memberId;
    private String username;
    private String password;
    private String email;
    private String name;
    private String phone;
    private Gender gender;
    private LocalDate birth;
    private Date regDate;
    private Date updateDate;

    private List<AuthVO> authList;
}

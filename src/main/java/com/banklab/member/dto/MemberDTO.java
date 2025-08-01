package com.banklab.member.dto;

import com.banklab.member.domain.Gender;
import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private String email;
    private String phone;
    private String name;
    private Gender gender;
    private String birth;
    private Date regDate;              // 등록일
    private Date updateDate;           // 수정일
    private List<String> authList;     // 권한 목록 (join 처리 필요)

    // MemberVO에서 DTO 생성 (정적 팩토리 메서드)
    public static MemberDTO of(MemberVO m) {
        return MemberDTO.builder()
                .email(m.getEmail())
                .phone(m.getPhone())
                .name(m.getName())
                .gender(m.getGender())
                .birth(String.valueOf(m.getBirth()))
                .regDate(m.getRegDate())
                .updateDate(m.getUpdateDate())
                .authList(m.getAuthList().stream()
                        .map(AuthVO::getAuth)
                        .toList())
                .build();
    }

    public MemberVO toVO() {
        return MemberVO.builder()
                .email(this.email)
                .name(this.name)
                .authList(authList.stream()
                        .map(auth -> new AuthVO(null, auth))
                        .toList())
                .build();
    }
}

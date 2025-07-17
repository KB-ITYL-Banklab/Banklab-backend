package com.banklab.member.dto;

import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private String username;
    private String email;
    private String phone;
    private String name;
    private Date regDate;              // 등록일
    private Date updateDate;           // 수정일
    private List<String> authList;     // 권한 목록 (join 처리 필요)

    // MemberVO에서 DTO 생성 (정적 팩토리 메서드)
    public static MemberDTO of(MemberVO m) {
        return MemberDTO.builder()
                .username(m.getUsername())
                .email(m.getEmail())
                .regDate(m.getRegDate())
                .updateDate(m.getUpdateDate())
                .authList(m.getAuthList().stream()
                        .map(a -> a.getAuth())
                        .toList())
                .build();
    }
}

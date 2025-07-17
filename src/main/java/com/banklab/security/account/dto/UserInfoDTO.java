package com.banklab.security.account.dto;

import com.banklab.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String username;
    private List<String> roles;

    /**
     * MemberVO에서 UserInfoDTO로 변환하는 팩토리 메서드
     * @param member
     * @return 변환된 UserInfoDTO
     */
    public static UserInfoDTO of(MemberVO member) {

        return new UserInfoDTO(
                member.getUsername(),
                member.getAuthList().stream()
                        .map(a -> a.getAuth())
                        .toList()
        );
    }
}


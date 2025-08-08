package com.banklab.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자산 새로고침, 삭제 요청 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountManageRequestDTO {

    private String bankCode;    // 기관코드
    private String connectedId;     // 커넥티드 아이디

}

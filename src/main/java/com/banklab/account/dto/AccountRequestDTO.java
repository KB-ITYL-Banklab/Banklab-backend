package com.banklab.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequestDTO {

    // 계좌 연동 시 사용
    private String bankCode;
    private String bankId;
    private String bankPassword;

    // 잔액 새로고침, 연동 해제 시 사용
    private String connectedId;
    
}

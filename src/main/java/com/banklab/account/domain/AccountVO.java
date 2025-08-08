package com.banklab.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountVO implements Serializable {

    private Long id;
    private Long memberId;    // 유저 아이디
    private String connectedId; // 유저가 발급한 커넥티드 아이디
    private String organization;  // 기관코드(은행)
    private String resAccountName;   // 계좌명
    private String resAccount;     // 계좌번호 (000000)
    private String resAccountDisplay;  // 계좌번호 표시용 (00-00-00)
    private String resAccountBalance;  // 잔액
    private String resAccountDeposit;   // 예금구분 (10:미분류, 11:수시입출금, 12:정기/적금, 13:신탁, 14:미분류)
    private String resAccountEndDate;     // 만기일 (적금인 경우)
    private String resAccountStartDate;   // 신규일
    private Date createdAt;

}

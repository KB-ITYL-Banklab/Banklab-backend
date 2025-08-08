package com.banklab.account.dto;

import com.banklab.account.domain.AccountVO;
import lombok.Data;

@Data
public class AccountDTO {
    public Long id;               // 계좌 테이블 기본키 ID 추가
    public String connectedId;    // 삭제 요청 시 필요
    public String organization;
    public String resAccount;
    public String resAccountName;
    public String resAccountDisplay;
    public String resAccountBalance;
    public String resAccountDeposit;      // 예금구분
    public String resAccountEndDate;        // 만기일
    public String resAccountStartDate;      // 신규일

    public AccountVO toVO(Long memberId, String connectedId, String organization) {
        return AccountVO.builder()
                .id(id)
                .memberId(memberId)
                .connectedId(connectedId)
                .organization(organization)
                .resAccount(resAccount)
                .resAccountName(resAccountName)
                .resAccountDisplay(resAccountDisplay)
                .resAccountBalance(resAccountBalance)
                .resAccountDeposit(resAccountDeposit)
                .resAccountEndDate(resAccountEndDate)
                .resAccountStartDate(resAccountStartDate)
                // id, createdAt은 DB에서 자동 처리
                .build();
    }
}

package com.banklab.account.dto;

import com.banklab.account.domain.AccountVO;
import lombok.Data;

@Data
public class AccountDTO {
    public String resAccount;
    public String resAccountName;
    public String resAccountDisplay;
    public String resAccountBalance;

    public AccountVO toVO(Long memberId, String connectedId, String organization) {
        return AccountVO.builder()
                .memberId(memberId)
                .connectedId(connectedId)
                .organization(organization)
                .resAccount(resAccount)
                .resAccountName(resAccountName)
                .resAccountDisplay(resAccountDisplay)
                .resAccountBalance(resAccountBalance)
                // id, createdAt은 DB에서 자동 처리
                .build();
    }
}

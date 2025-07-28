package com.banklab.stock.dto;

import com.banklab.stock.domain.StockVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDTO {
    // 계좌 정보
    private String resAccount;              // 계좌번호
    private String resDepositReceived;      // 예수금

    // 보유 종목 정보
    private String resItemName;             // 종목명
    private String resItemCode;             // 종목코드
    private String resQuantity;             // 보유수량
    private String resValuationAmt;         // 평가금액
    private String resPurchaseAmount;       // 매입금액
    private String resValuationPL;          // 평가손익
    private String resEarningsRate;         // 수익률

    // 삭제 시 필요
    private String connectedId;
    private String organization;

    public StockVO toVO(Long memberId, String connectedId, String organization) {
        return StockVO.builder()
                .memberId(memberId)
                .connectedId(connectedId)
                .organization(organization)
                .resAccount(resAccount)
                .resDepositReceived(resDepositReceived)
                .resItemName(resItemName)
                .resItemCode(resItemCode)
                .resQuantity(resQuantity)
                .resValuationAmt(resValuationAmt)
                .resPurchaseAmount(resPurchaseAmount)
                .resValuationPL(resValuationPL)
                .resEarningsRate(resEarningsRate)
                .build();
    }
}
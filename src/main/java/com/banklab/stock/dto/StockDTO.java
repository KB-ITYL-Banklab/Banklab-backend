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
    private String resDepositReceivedD1;    // 예수금(D+1)
    private String resDepositReceivedD2;    // 예수금(D+2)

    // 보유 종목 정보
    private String resProductType;          // 상품유형
    private String resItemName;             // 상품명
    private String resItemCode;             // 상품코드
    private String resQuantity;             // 보유수량
    private String resPresentAmt;           // 현재가
    private String resPurchaseAmount;       // 매입금액
    private String resValuationAmt;         // 평가금액
    private String resValuationPL;          // 평가손익
    private String resEarningsRate;         // 수익률
    private String resAccountCurrency;      // 통화코드

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
                .resDepositReceivedD1(resDepositReceivedD1)
                .resDepositReceivedD2(resDepositReceivedD2)
                .resProductType(resProductType)
                .resItemName(resItemName)
                .resItemCode(resItemCode)
                .resQuantity(resQuantity)
                .resPresentAmt(resPresentAmt)
                .resPurchaseAmount(resPurchaseAmount)
                .resValuationAmt(resValuationAmt)
                .resValuationPL(resValuationPL)
                .resEarningsRate(resEarningsRate)
                .resAccountCurrency(resAccountCurrency)
                .build();
    }
}
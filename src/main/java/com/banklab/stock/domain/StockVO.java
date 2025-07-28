package com.banklab.stock.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockVO {

    private Long id;
    private Long memberId;    // 유저 아이디
    private String connectedId; // 유저가 발급한 커넥티드 아이디
    private String organization;  // 기관코드(은행)

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

    private Date updatedAt;
}

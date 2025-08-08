package com.banklab.financeContents.dto;

import lombok.Data;

/**
 * 업비트 마켓 정보 DTO
 */
@Data
public class UpbitMarketDto {
    private String market;          // 업비트에서 제공중인 시장 정보 (ex: KRW-BTC)
    private String korean_name;     // 거래 대상 코인명 (ex: 비트코인)
    private String english_name;    // 거래 대상 코인명 (ex: Bitcoin)
    private String market_warning;  // 유의종목 여부 (NONE, CAUTION)
}

package com.banklab.financeContents.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 업비트 금융 데이터 도메인 (Spring Legacy 호환)
 */
@Data
public class FinanceUpbit {
    private Long id;                        // 고유 ID
    private String market;                  // 마켓 코드 (예: KRW-BTC)
    private Date candleDateTime;            // 캔들 기준 시각 (실제 캔들의 날짜/시간)
    private BigDecimal openingPrice;        // 시가 (DECIMAL)
    private BigDecimal tradePrice;          // 종가 (현재가, DECIMAL)
    private BigDecimal prevClosingPrice;    // 전일 종가 (DECIMAL)
    private Double changeRate;              // 전일 대비 등락률 (비율)
    private Double accTradeVolume24h;       // 24시간 누적 거래량 (DOUBLE 유지)
    private Double accTradePrice24h;        // 24시간 누적 거래대금 (DOUBLE 유지)
    private Date createdAt;                 // 데이터 생성 시각
    private Date updatedAt;                 // 데이터 수정 시각
}

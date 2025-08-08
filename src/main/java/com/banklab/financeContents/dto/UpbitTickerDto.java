package com.banklab.financeContents.dto;

import lombok.Data;

/**
 * 업비트 Ticker API 응답 DTO
 */
@Data
public class UpbitTickerDto {
    private String market;                    // 마켓 코드
    private String trade_date;               // 최근 거래 일자(UTC)
    private String trade_time;               // 최근 거래 시각(UTC)
    private String trade_date_kst;           // 최근 거래 일자(KST)
    private String trade_time_kst;           // 최근 거래 시각(KST)
    private Long trade_timestamp;            // 최근 거래 일시(timestamp)
    private Double opening_price;            // 시가
    private Double high_price;               // 고가
    private Double low_price;                // 저가
    private Double trade_price;              // 종가(현재가)
    private Double prev_closing_price;       // 전일 종가
    private String change;                   // 전일 대비 (EVEN, RISE, FALL)
    private Double change_price;             // 전일 대비 값
    private Double change_rate;              // 전일 대비 등락률
    private Double signed_change_price;      // 부호가 있는 전일 대비 값
    private Double signed_change_rate;       // 부호가 있는 전일 대비 등락률
    private Double trade_volume;             // 가장 최근 거래량
    private Double acc_trade_price;          // 누적 거래대금(UTC 0시 기준)
    private Double acc_trade_price_24h;      // 24시간 누적 거래대금
    private Double acc_trade_volume;         // 누적 거래량(UTC 0시 기준)
    private Double acc_trade_volume_24h;     // 24시간 누적 거래량
    private Double highest_52_week_price;    // 52주 신고가
    private String highest_52_week_date;     // 52주 신고가 달성일
    private Double lowest_52_week_price;     // 52주 신저가
    private String lowest_52_week_date;      // 52주 신저가 달성일
    private Long timestamp;                  // 타임스탬프
}

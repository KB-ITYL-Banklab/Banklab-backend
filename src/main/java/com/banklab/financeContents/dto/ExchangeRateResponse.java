package com.banklab.financeContents.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * 환율 API 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponse {
    
    /**
     * 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 환율 데이터 리스트
     */
    private List<ExchangeRateDto> data;
    
    /**
     * 조회 날짜 (YYYYMMDD 형식)
     */
    private String searchDate;
    
    /**
     * 조회된 데이터 개수
     */
    private int count;
}

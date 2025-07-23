package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.ExchangeRateResponse;
import com.banklab.financeContents.service.ExchangeRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 환율 정보 REST API 컨트롤러
 * 
 * 제공 기능:
 * - GET /api/exchange-rate/today : 오늘 환율 조회
 * - GET /api/exchange-rate/date/{date} : 특정 날짜 환율 조회  
 * - GET /api/exchange-rate/date/{date}/currency/{code} : 특정 통화 환율 조회
 * 
 * 데이터 소스: 한국수출입은행 환율 API
 */
@Slf4j
@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@Api(tags = "환율 정보 API", description = "한국수출입은행 환율 정보 조회")
public class ExchangeRateController {
    
    private final ExchangeRateService exchangeRateService;

    
    /**
     * 특정 날짜, 특정 통화 환율 정보 조회 API
     * 지정된 날짜의 특정 통화 환율 정보만 반환합니다.
     */
    @GetMapping("/chart")
    @ApiOperation(value = "웹페이지 차트용 환율 정보 조회")
    public ResponseEntity<Map<String, Object>> getExchangeRateForChart() {
        try {
            log.info("📊 차트용 환율 정보 조회 요청");
            
            ExchangeRateResponse response = exchangeRateService.getTodayExchangeRates();
            
            if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                List<Map<String, Object>> chartData = response.getData().stream()
                    .map(exchange -> {
                        Map<String, Object> chartItem = new HashMap<>();
                        chartItem.put("name", exchange.getCur_nm());
                        chartItem.put("currentPrice", exchange.getDeal_bas_r());
                        chartItem.put("updateDate", response.getSearchDate());
                        return chartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                Map<String, Object> result = new HashMap<>();
                result.put("data", chartData);
                result.put("count", chartData.size());
                result.put("message", "차트용 환율 정보 조회 성공");
                
                log.info("✅ 차트용 환율 정보 조회 성공: {}개", chartData.size());
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", response.getMessage());
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 차트용 환율 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 날짜 형식 검증 유틸리티 메서드
     * YYYYMMDD 형식의 8자리 숫자인지 확인합니다.
     */
    private boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.length() != 8) {
            return false;
        }
        
        try {
            Integer.parseInt(dateString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

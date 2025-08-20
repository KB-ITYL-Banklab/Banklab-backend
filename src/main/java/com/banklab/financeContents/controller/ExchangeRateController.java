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
    @ApiOperation(value = "웹페이지 차트용 환율 정보 조회 (주요 5개 통화)", notes = "주요 5개 통화의 환율 정보를 차트 형식으로 조회.")
    public ResponseEntity<Map<String, Object>> getExchangeRateForChart() {
        try {
            // log.info("📊 차트용 환율 정보 조회 요청 (주요 5개 통화)");
            
            ExchangeRateResponse response = exchangeRateService.getTodayExchangeRates();
            
            if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                // 주요 5개 통화만 필터링
                List<String> targetCurrencies = java.util.Arrays.asList(
                    "미국 달러", "유로", "영국 파운드", "호주 달러", "일본 옌"
                );
                
                List<Map<String, Object>> chartData = response.getData().stream()
                    .filter(exchange -> targetCurrencies.contains(exchange.getCur_nm()))
                    .map(exchange -> {
                        Map<String, Object> chartItem = new HashMap<>();
                        chartItem.put("name", exchange.getCur_nm());
                        chartItem.put("currentPrice", exchange.getDeal_bas_r());
                        chartItem.put("updateDate", response.getSearchDate());
                        return chartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                // log.info("필터링된 통화 수: {} / 전체: {}", chartData.size(), response.getData().size());
                
                Map<String, Object> result = new HashMap<>();
                result.put("data", chartData);
                result.put("count", chartData.size());
                result.put("message", "차트용 환율 정보 조회 성공");
                
                // log.info("✅ 차트용 환율 정보 조회 성공: {}개", chartData.size());
                return ResponseEntity.ok(result);
            } else {
                // log.warn("⚠️ 환율 서비스에서 데이터를 가져오지 못했습니다");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", response.getMessage());
                errorResponse.put("service", "ExchangeRateService");
                return ResponseEntity.status(503).body(errorResponse);
            }
        } catch (Exception e) {
            // log.error("❌ 차트용 환율 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("service", "ExchangeRateService");
            return ResponseEntity.status(500).body(errorResponse);
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

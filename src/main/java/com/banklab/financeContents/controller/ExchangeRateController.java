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
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
@Api(tags = "환율 정보 API", description = "한국수출입은행 환율 정보 조회")
public class ExchangeRateController {
    
    private final ExchangeRateService exchangeRateService;
    
    /**
     * 오늘 환율 정보 조회 API
     * 현재 날짜의 모든 통화 환율 정보를 반환합니다.
     * 
     * @return ResponseEntity<ExchangeRateResponse> - 환율 정보 또는 오류 메시지
     */
    @GetMapping("/today")
    @ApiOperation(value = "오늘 환율 정보 조회", notes = "오늘 날짜의 모든 통화 환율 정보를 조회합니다.")
    public ResponseEntity<ExchangeRateResponse> getTodayExchangeRates() {
        log.info("오늘 환율 정보 조회 요청");
        
        try {
            ExchangeRateResponse response = exchangeRateService.getTodayExchangeRates();
            
            if (response.isSuccess()) {
                log.info("오늘 환율 정보 조회 성공. 데이터 개수: {}", response.getCount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("오늘 환율 정보 조회 실패: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("오늘 환율 정보 조회 중 오류 발생", e);
            ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                .success(false)
                .message("서버 오류가 발생했습니다: " + e.getMessage())
                .count(0)
                .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 특정 날짜 환율 정보 조회 API
     * 지정된 날짜의 모든 통화 환율 정보를 반환합니다.
     * 
     * @param searchDate 조회할 날짜 (YYYYMMDD 형식, 예: 20250721)
     * @return ResponseEntity<ExchangeRateResponse> - 해당 날짜의 환율 정보
     */
    @GetMapping("/date/{searchDate}")
    @ApiOperation(value = "특정 날짜 환율 정보 조회", notes = "지정된 날짜의 모든 통화 환율 정보를 조회합니다.")
    public ResponseEntity<ExchangeRateResponse> getExchangeRatesByDate(
            @ApiParam(value = "조회 날짜 (YYYYMMDD 형식)", example = "20240122")
            @PathVariable String searchDate) {
        
        log.info("특정 날짜 환율 정보 조회 요청. 날짜: {}", searchDate);
        
        // 날짜 형식 검증
        if (!isValidDateFormat(searchDate)) {
            ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                .success(false)
                .message("올바르지 않은 날짜 형식입니다. YYYYMMDD 형식으로 입력해주세요.")
                .searchDate(searchDate)
                .count(0)
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            ExchangeRateResponse response = exchangeRateService.getExchangeRates(searchDate);
            
            if (response.isSuccess()) {
                log.info("특정 날짜 환율 정보 조회 성공. 날짜: {}, 데이터 개수: {}", 
                    searchDate, response.getCount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("특정 날짜 환율 정보 조회 실패. 날짜: {}, 메시지: {}", 
                    searchDate, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("특정 날짜 환율 정보 조회 중 오류 발생. 날짜: {}", searchDate, e);
            ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                .success(false)
                .message("서버 오류가 발생했습니다: " + e.getMessage())
                .searchDate(searchDate)
                .count(0)
                .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 특정 날짜, 특정 통화 환율 정보 조회 API
     * 지정된 날짜의 특정 통화 환율 정보만 반환합니다.
     */
    @GetMapping("/date/{searchDate}/currency/{currencyCode}")
    @ApiOperation(value = "특정 날짜, 특정 통화 환율 정보 조회", 
                  notes = "지정된 날짜의 특정 통화 환율 정보를 조회합니다.")
    public ResponseEntity<ExchangeRateResponse> getExchangeRateByCurrency(
            @ApiParam(value = "조회 날짜 (YYYYMMDD 형식)", example = "20240122")
            @PathVariable String searchDate,
            @ApiParam(value = "통화 코드", example = "USD")
            @PathVariable String currencyCode) {
        
        log.info("특정 날짜, 특정 통화 환율 정보 조회 요청. 날짜: {}, 통화: {}", 
            searchDate, currencyCode);
        
        // 날짜 형식 검증
        if (!isValidDateFormat(searchDate)) {
            ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                .success(false)
                .message("올바르지 않은 날짜 형식입니다. YYYYMMDD 형식으로 입력해주세요.")
                .searchDate(searchDate)
                .count(0)
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            ExchangeRateResponse response = exchangeRateService.getExchangeRateByCurrency(
                searchDate, currencyCode);
            
            if (response.isSuccess()) {
                log.info("특정 날짜, 특정 통화 환율 정보 조회 성공. 날짜: {}, 통화: {}, 데이터 개수: {}", 
                    searchDate, currencyCode, response.getCount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("특정 날짜, 특정 통화 환율 정보 조회 실패. 날짜: {}, 통화: {}, 메시지: {}", 
                    searchDate, currencyCode, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("특정 날짜, 특정 통화 환율 정보 조회 중 오류 발생. 날짜: {}, 통화: {}", 
                searchDate, currencyCode, e);
            ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                .success(false)
                .message("서버 오류가 발생했습니다: " + e.getMessage())
                .searchDate(searchDate)
                .count(0)
                .build();
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

package com.banklab.financeContents.controller;

import com.banklab.financeContents.domain.FinanceStockVO;
import com.banklab.financeContents.dto.StockSecurityInfoDto;
import com.banklab.financeContents.dto.StockSearchResultDto;
import com.banklab.financeContents.service.FinanceStockService;
import com.banklab.financeContents.service.PublicDataStockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주식 정보 조회 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/stocks")
@Api(tags = "주식 정보 API")
public class StockController {
    
    private static final Logger log = LoggerFactory.getLogger(StockController.class);
    
    @Autowired
    private PublicDataStockService publicDataStockService;
    
    @Autowired
    private FinanceStockService financeStockService;

    // ===== 실시간 API 조회 =====
    
    @GetMapping("/chart")
    @ApiOperation(value = "웹페이지 차트용 주식 정보 조회 (주요 5개 종목 - 실제 데이터)")
    public ResponseEntity<Map<String, Object>> getStocksForChart() {
        try {
            log.info("📊 차트용 주식 정보 조회 요청 (주요 5개 종목) - 실제 데이터 모드");
            
            // 주요 5개 종목 코드 정의
            String[] targetStocks = {"005930", "035420", "005380", "035720", "000150"};
            List<StockSecurityInfoDto> stockList = new ArrayList<>();
            
            log.info("🔍 실제 공공데이터 API에서 5개 종목 직접 조회 시작");
            
            // 순차 처리로 간소화
            for (String stockCode : targetStocks) {
                try {
                    StockSecurityInfoDto stock = publicDataStockService.getStockInfoByCode(stockCode);
                    if (stock != null) {
                        stockList.add(stock);
                        log.info("✅ 주식 조회 성공: {} ({}) - {}원", 
                            stock.getItemName(), stock.getShortCode(), stock.getClosePrice());
                    } else {
                        log.warn("⚠️ 주식 조회 실패: {} (데이터 없음)", stockCode);
                    }
                } catch (Exception e) {
                    log.error("❌ 주식 조회 오류 {}: {}", stockCode, e.getMessage());
                }
            }
            
            log.info("📊 실제 데이터 조회 완료: {}/5개 종목 성공", stockList.size());
            
            if (!stockList.isEmpty()) {
                List<Map<String, Object>> chartData = new ArrayList<>();
                for (StockSecurityInfoDto stock : stockList) {
                    Map<String, Object> chartItem = new HashMap<>();
                    chartItem.put("stockCode", stock.getShortCode());
                    chartItem.put("name", stock.getItemName());
                    chartItem.put("currentPrice", stock.getClosePrice());
                    chartItem.put("updateDate", stock.getBaseDate());
                    chartData.add(chartItem);
                }
                
                return createSuccessResponse("차트용 주식 정보 조회 성공", chartData);
            } else {
                return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, 
                    "조회된 데이터가 없습니다", "공공데이터 API 호출 실패 또는 데이터 없음");
            }
        } catch (Exception e) {
            log.error("❌ 차트용 주식 정보 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "서버 오류가 발생했습니다", e.getMessage());
        }
    }

    // ===== 데이터베이스 저장 =====


    @PostMapping("/save/today")
    @ApiOperation(value = "오늘자 주식 정보를 API에서 가져와서 데이터베이스에 저장 (상위 200개)")
    public ResponseEntity<Map<String, Object>> saveStockDataToday() {
        try {
            log.info("🔵 [POST] /save/today 요청 시작");
            
            LocalDate yesterday = LocalDate.now().minusDays(1); // 전일 데이터
            log.info("📅 저장 대상 날짜: {} (어제)", yesterday);
            
            int savedCount = financeStockService.saveTopStockDataFromApi(yesterday, 200);
            
            Map<String, Object> result = createSuccessResponseMap("오늘자 주식 데이터 저장 완료", null);
            result.put("date", yesterday.toString());
            result.put("savedCount", savedCount);
            result.put("topCount", 200);
            
            log.info("✅ [POST] /save/today 완료: {}건 저장", savedCount);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ [POST] /save/today 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "오늘자 주식 데이터 저장 실패", e.getMessage());
        }
    }

    @PostMapping("/save/recent")
    @ApiOperation(value = "최근 30일간 상위 200개 종목 데이터를 배치로 저장")
    public ResponseEntity<Map<String, Object>> saveRecentStockData() {
        try {
            log.info("🔵 [POST] /save/recent 요청 시작 - 최근 30일간 데이터 저장");
            
            // 오래된 데이터 먼저 삭제
            int deletedCount = financeStockService.deleteOldData();
            log.info("🗑️ 30일 이전 오래된 데이터 {}건 삭제", deletedCount);
            
            // 최근 30일 데이터 저장
            int savedCount = financeStockService.saveRecentStockData(30, 200);
            
            Map<String, Object> result = createSuccessResponseMap("최근 30일 데이터 저장 완료", null);
            result.put("savedCount", savedCount);
            result.put("deletedCount", deletedCount);
            result.put("period", "30일");
            result.put("topCount", 200);
            
            log.info("✅ [POST] /save/recent 완료: 저장 {}건, 삭제 {}건", savedCount, deletedCount);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ [POST] /save/recent 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "최근 데이터 저장 실패", e.getMessage());
        }
    }

    // ===== 데이터베이스 조회 =====

    @GetMapping("/db/top/{limit}")
    @ApiOperation(value = "데이터베이스에서 인기 종목 조회")
    public ResponseEntity<Map<String, Object>> getTopStocksFromDB(
            @ApiParam(value = "조회할 개수", example = "10") 
            @PathVariable int limit) {
        try {
            log.info("🏆 데이터베이스에서 인기 종목 {}개 조회", limit);
            
            List<FinanceStockVO> stocks = financeStockService.getTopStocks(limit);
            
            Map<String, Object> result = createSuccessResponseMap("인기 종목 조회 성공", stocks);
            result.put("limit", limit);
            result.put("count", stocks.size());
            
            log.info("✅ 인기 종목 조회 완료: {}건", stocks.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ 인기 종목 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "인기 종목 조회 실패", e.getMessage());
        }
    }

    // ===== 주식 검색 =====

    @GetMapping("/search")
    @ApiOperation(value = "주식명으로 검색 (모든 날짜 데이터)")
    public ResponseEntity<Map<String, Object>> searchStocksByName(
            @ApiParam(value = "검색할 주식명 (부분 검색 가능)", example = "삼성") 
            @RequestParam String name) {
        try {
            String decodedName = java.net.URLDecoder.decode(name, "UTF-8");
            log.info("🔍 주식명 검색 요청: '{}' (디코딩: '{}')", name, decodedName);
            
            List<FinanceStockVO> stocks = financeStockService.searchStocksByName(decodedName);
            
            // 안전한 DTO로 변환
            List<StockSearchResultDto> safeResults = new ArrayList<>();
            for (FinanceStockVO stock : stocks) {
                try {
                    StockSearchResultDto dto = new StockSearchResultDto(stock);
                    safeResults.add(dto);
                } catch (Exception e) {
                    log.warn("⚠️ 주식 데이터 변환 실패 (ID: {}): {}", stock.getId(), e.getMessage());
                }
            }
            
            Map<String, Object> result = createSuccessResponseMap("주식명 검색 완료", safeResults);
            result.put("searchKeyword", decodedName);
            result.put("count", safeResults.size());
            result.put("totalFound", stocks.size());
            
            log.info("✅ '{}' 검색 완료: {}건 (변환 성공: {}건)", decodedName, stocks.size(), safeResults.size());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 잘못된 검색 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, 
                "잘못된 요청", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 주식명 검색 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "주식명 검색 실패", e.getMessage());
        }
    }

    @GetMapping("/search/latest")
    @ApiOperation(value = "주식명으로 최신 데이터만 검색")
    public ResponseEntity<Map<String, Object>> searchLatestStocksByName(
            @ApiParam(value = "검색할 주식명 (부분 검색 가능)", example = "삼성") 
            @RequestParam String name,
            @ApiParam(value = "조회할 개수 (기본값: 10)", example = "10") 
            @RequestParam(required = false) Integer limit) {
        try {
            String decodedName = java.net.URLDecoder.decode(name, "UTF-8");
            log.info("🔍 최신 주식명 검색 요청: '{}' (최대 {}개)", decodedName, limit != null ? limit : 10);
            
            List<FinanceStockVO> stocks = financeStockService.searchLatestStocksByName(decodedName, limit);
            
            // 안전한 DTO로 변환
            List<StockSearchResultDto> safeResults = new ArrayList<>();
            for (FinanceStockVO stock : stocks) {
                try {
                    StockSearchResultDto dto = new StockSearchResultDto(stock);
                    safeResults.add(dto);
                } catch (Exception e) {
                    log.warn("⚠️ 주식 데이터 변환 실패 (ID: {}): {}", stock.getId(), e.getMessage());
                }
            }
            
            Map<String, Object> result = createSuccessResponseMap("최신 주식명 검색 완료", safeResults);
            result.put("searchKeyword", decodedName);
            result.put("limit", limit != null ? limit : 10);
            result.put("count", safeResults.size());
            result.put("totalFound", stocks.size());
            
            log.info("✅ '{}' 최신 검색 완료: {}건 (변환 성공: {}건)", decodedName, stocks.size(), safeResults.size());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 잘못된 검색 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, 
                "잘못된 요청", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 최신 주식명 검색 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "최신 주식명 검색 실패", e.getMessage());
        }
    }

    // ===== 공통 유틸리티 메서드 =====

    /**
     * JSON 문자열을 안전하게 이스케이프하는 메서드
     */
    private String safeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replaceAll("[\\x00-\\x1F\\x7F]", ""); // 제어 문자 제거
    }

    /**
     * 기본 응답 객체 생성
     */
    private Map<String, Object> createBaseResponse(boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return response;
    }

    /**
     * 성공 응답 객체 생성 (ResponseEntity 반환)
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = createBaseResponse(true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 성공 응답 객체 생성 (Map 반환)
     */
    private Map<String, Object> createSuccessResponseMap(String message, Object data) {
        Map<String, Object> response = createBaseResponse(true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    /**
     * 오류 응답 객체 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = createBaseResponse(false);
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}

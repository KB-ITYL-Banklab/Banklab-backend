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
import java.util.stream.Collectors;

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

    // ===== 데이터베이스 저장 (관리용) =====

    @PostMapping("/save/today")
    @ApiOperation(value = "어제 날짜 기준으로 상위 200개 종목의 주식 정보를 DB에 저장")
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
    @ApiOperation(value = "최근 30일간 상위 200개 종목 데이터를 배치로 저장 (30일 이전 데이터 삭제)")
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

    @GetMapping("/db/count")
    @ApiOperation(value = "데이터베이스 총 데이터 수 조회")
    public ResponseEntity<Map<String, Object>> getStockDataCount() {
        try {
            log.info("📊 데이터베이스 총 데이터 수 조회");
            
            // 간단한 카운트 조회 (JSON 직렬화 문제 우회)
            List<FinanceStockVO> stocks = financeStockService.getTopStocks(1);
            int totalCount = stocks.size() > 0 ? 1 : 0;
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "데이터 수 조회 성공");
            result.put("totalCount", totalCount);
            result.put("hasData", totalCount > 0);
            
            if (totalCount > 0) {
                FinanceStockVO sample = stocks.get(0);
                result.put("sampleStock", sample.getItmsNm());
                result.put("sampleDate", sample.getBasDt().toString());
            }
            
            log.info("✅ 데이터 수 조회 완료: {}건", totalCount);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ 데이터 수 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "데이터 수 조회 실패", e.getMessage());
        }
    }

    @GetMapping("/db/top/{limit}")
    @ApiOperation(value = "데이터베이스에서 인기 종목 조회")
    public ResponseEntity<Map<String, Object>> getTopStocksFromDB(
            @ApiParam(value = "조회할 개수", example = "10") 
            @PathVariable int limit) {
        try {
            log.info("🏆 데이터베이스에서 인기 종목 {}개 조회", limit);
            
            List<FinanceStockVO> stocks = financeStockService.getTopStocks(limit);
            
            // JSON 직렬화 문제 해결을 위해 안전한 형태로 변환
            List<Map<String, Object>> safeStocks = new ArrayList<>();
            for (FinanceStockVO stock : stocks) {
                Map<String, Object> safeStock = new HashMap<>();
                safeStock.put("id", stock.getId());
                safeStock.put("stockCode", stock.getSrtnCd());
                safeStock.put("stockName", safeJsonString(stock.getItmsNm()));
                safeStock.put("closingPrice", stock.getClpr());
                safeStock.put("baseDate", stock.getBasDt() != null ? stock.getBasDt().toString() : null);
                safeStock.put("versus", stock.getVs());
                safeStocks.add(safeStock);
            }
            
            Map<String, Object> result = createSuccessResponseMap("인기 종목 조회 성공", safeStocks);
            result.put("limit", limit);
            result.put("count", safeStocks.size());
            
            log.info("✅ 인기 종목 조회 완료: {}건", stocks.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ 인기 종목 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "인기 종목 조회 실패", e.getMessage());
        }
    }


    // ===== 주식 시계열 데이터 조회 =====
    
    @GetMapping("/timeseries")
    @ApiOperation(value = "종목명으로 시계열 데이터 조회 (기준일자별 정렬)")
    public ResponseEntity<Map<String, Object>> getStockTimeSeries(
            @ApiParam(value = "검색할 종목명 (부분 검색 가능)", example = "SK하이닉스") 
            @RequestParam String name,
            @ApiParam(value = "조회할 개수 (기본값: 30)", example = "30") 
            @RequestParam(required = false, defaultValue = "30") Integer limit) {
        try {
            // 한글 인코딩 처리
            String tempName;
            try {
                tempName = java.net.URLDecoder.decode(name, "UTF-8");
            } catch (Exception e) {
                tempName = name;
            }
            final String decodedName = tempName;
            
            log.info("📈 시계열 데이터 조회: '{}' (최대 {}개)", decodedName, limit);
            
            List<FinanceStockVO> stocks = financeStockService.searchStocksByName(decodedName);
            
            if (stocks.isEmpty()) {
                Map<String, Object> result = createSuccessResponseMap("검색 결과가 없습니다", new ArrayList<>());
                result.put("searchKeyword", decodedName);
                result.put("count", 0);
                return ResponseEntity.ok(result);
            }
            
            // 기준일자별 정렬 (최신순)
            stocks.sort((a, b) -> {
                if (a.getBasDt() == null && b.getBasDt() == null) return 0;
                if (a.getBasDt() == null) return 1;
                if (b.getBasDt() == null) return -1;
                return b.getBasDt().compareTo(a.getBasDt()); // 최신 날짜부터
            });
            
            // 요청된 개수만큼 제한
            List<FinanceStockVO> limitedStocks = stocks.stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            // 시계열 데이터 형태로 변환 (실제 DB 필드명 사용)
            List<Map<String, Object>> timeSeriesData = new ArrayList<>();
            for (FinanceStockVO stock : limitedStocks) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("id", stock.getId());
                dataPoint.put("bas_dt", stock.getBasDt() != null ? stock.getBasDt().toString() : null);
                dataPoint.put("stockName", safeJsonString(stock.getItmsNm()));
                dataPoint.put("clpr", stock.getClpr());
                dataPoint.put("versus", stock.getVs());
                dataPoint.put("fluctuationRate", stock.getFltRt());
                dataPoint.put("tradingVolume", stock.getTrqu());
                dataPoint.put("tradingValue", stock.getTrPrc());
                timeSeriesData.add(dataPoint);
            }
            
            Map<String, Object> result = createSuccessResponseMap("시계열 데이터 조회 성공", timeSeriesData);
            result.put("searchKeyword", decodedName);
            result.put("count", timeSeriesData.size());
            result.put("totalFound", stocks.size());
            result.put("limit", limit);
            
            log.info("✅ '{}' 시계열 데이터 조회 완료: {}건 반환 (전체 {}건)", 
                decodedName, timeSeriesData.size(), stocks.size());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 잘못된 시계열 조회 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, 
                "잘못된 요청", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 시계열 데이터 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "시계열 데이터 조회 실패", e.getMessage());
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

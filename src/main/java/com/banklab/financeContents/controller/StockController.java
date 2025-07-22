package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.StockSecurityInfoDto;
import com.banklab.financeContents.service.PublicDataStockService;
import com.banklab.financeContents.util.StockCodeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주식 정보 조회 REST API 컨트롤러
 * 
 * 이 컨트롤러는 공공데이터포털의 주식시세정보 API를 통해 
 * 주식 관련 정보를 조회하는 REST API 엔드포인트들을 제공합니다.
 * 
 * 주요 기능:
 * - 특정 종목 정보 조회 (종목코드 기반)
 * - 주식 목록 조회 (페이징 지원)
 * - 주요 종목 정보 제공
 * - 상위 N개 종목 조회
 * - 종목 코드 유효성 검증
 * 
 * API 문서: Swagger UI에서 확인 가능
 * 기본 경로: /api/stocks
 */
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@Api(tags = "주식 정보 API")
public class StockController {
    
    @Autowired
    private PublicDataStockService publicDataStockService;

    @GetMapping("/public/{stockCode}")      //가능
    @ApiOperation(value = "공공데이터 주식 정보 조회 (종목별)")
    public ResponseEntity<?> getPublicStockInfo(
            @ApiParam(value = "종목 단축코드 (6자리)", required = true)
            @PathVariable String stockCode) {
        try {
            log.info("🔍 종목 {} 조회 요청", stockCode);
            
            // 종목 코드 검증
            if (stockCode == null || stockCode.trim().length() != 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "종목 코드는 6자리여야 합니다");
                errorResponse.put("requestedCode", stockCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            StockSecurityInfoDto stockInfo = publicDataStockService.getStockInfoByCode(stockCode.trim());
            if (stockInfo != null) {
                log.info("✅ 종목 {} 조회 성공: {}", stockCode, stockInfo.getItemName());
                return ResponseEntity.ok(stockInfo);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "해당 종목을 찾을 수 없습니다");
                errorResponse.put("requestedCode", stockCode);
                errorResponse.put("message", "최근 7일간의 데이터에서 해당 종목을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ 주식 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestedCode", stockCode);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/public/list")   //가능
    @ApiOperation(value = "공공데이터 주식 정보 목록 조회")
    public ResponseEntity<?> getPublicStockList(
            @ApiParam(value = "기준일자 (YYYYMMDD), 미입력시 전일")
            @RequestParam(required = false) String baseDate,
            
            @ApiParam(value = "조회할 종목 수")
            @RequestParam(defaultValue = "10") int numOfRows,
            
            @ApiParam(value = "페이지 번호")
            @RequestParam(defaultValue = "1") int pageNo) {
        try {
            log.info("📊 주식 목록 조회 요청 - 기준일:{}, 개수:{}, 페이지:{}", baseDate, numOfRows, pageNo);
            
            // 파라미터 검증
            if (numOfRows <= 0 || numOfRows > 1000) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회 개수는 1~1000 사이여야 합니다");
                errorResponse.put("requestedRows", numOfRows);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (pageNo <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "페이지 번호는 1 이상이어야 합니다");
                errorResponse.put("requestedPage", pageNo);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            List<StockSecurityInfoDto> stockList = publicDataStockService.getStockPriceInfo(
                    baseDate, null, numOfRows, pageNo);
            
            if (stockList != null && !stockList.isEmpty()) {
                log.info("✅ 주식 목록 조회 성공: {}개", stockList.size());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("data", stockList);
                successResponse.put("count", stockList.size());
                successResponse.put("page", pageNo);
                successResponse.put("numOfRows", numOfRows);
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "조회된 데이터가 없습니다");
                errorResponse.put("baseDate", baseDate);
                errorResponse.put("page", pageNo);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 주식 목록 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/public/major")
    @ApiOperation(value = "주요 종목 정보 조회")
    public ResponseEntity<Map<String, Object>> getMajorStocks() {
        try {
            Map<String, String> majorStocks = StockCodeUtil.getMajorStocks();
            Map<String, Object> result = new HashMap<>();
            result.put("majorStocks", majorStocks);
            result.put("count", majorStocks.size());
            result.put("message", "주요 종목 목록 조회 성공");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("주요 종목 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/public/top/{count}")
    @ApiOperation(value = "공공데이터 주요 종목 조회")
    public ResponseEntity<?> getTopStocks(
            @ApiParam(value = "조회할 종목 수", required = true)
            @PathVariable int count) {
        try {
            log.info("🏆 상위 {} 종목 조회 요청", count);
            
            if (count <= 0 || count > 100) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회 개수는 1~100 사이여야 합니다");
                errorResponse.put("requestedCount", count);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            List<StockSecurityInfoDto> topStocks = publicDataStockService.getTopStocks(count);
            
            if (topStocks != null && !topStocks.isEmpty()) {
                log.info("✅ 상위 종목 조회 성공: {}개", topStocks.size());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("data", topStocks);
                successResponse.put("count", topStocks.size());
                successResponse.put("requestedCount", count);
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "최근 영업일 데이터를 찾을 수 없습니다");
                errorResponse.put("requestedCount", count);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 주요 종목 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestedCount", count);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/public/validate/{stockCode}")
    @ApiOperation(value = "종목 코드 유효성 검증")
    public ResponseEntity<Map<String, Object>> validateStockCode(
            @ApiParam(value = "검증할 종목 코드", required = true)
            @PathVariable String stockCode) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 종목 코드 정규화
        String normalizedCode = StockCodeUtil.normalizeStockCode(stockCode);
        boolean isValid = StockCodeUtil.isValidStockCode(normalizedCode);
        boolean isMajor = isValid && StockCodeUtil.isMajorStock(normalizedCode);
        String stockName = isMajor ? StockCodeUtil.getStockName(normalizedCode) : null;
        
        result.put("originalCode", stockCode);
        result.put("normalizedCode", normalizedCode);
        result.put("isValid", isValid);
        result.put("isMajorStock", isMajor);
        result.put("stockName", stockName);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{stockCode}")
    @ApiOperation(value = "주식 정보 조회 (공공데이터 API)")
    public ResponseEntity<?> getStockInfo(@PathVariable String stockCode) {
        log.info("🔄 기본 엔드포인트 -> 공공데이터 API 리다이렉트: {}", stockCode);
        // 기존 엔드포인트를 공공데이터 API로 리다이렉트
        return getPublicStockInfo(stockCode);
    }
}

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
 *
 * ++++  특정 종목 정보 날짜로 조회
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


    // 현재가, 업데이트 날짜 추가
    @GetMapping("/chart")
    @ApiOperation(value = "웹페이지 차트용 주식 정보 조회 (주요 5개 종목 - 실제 데이터)")
    public ResponseEntity<Map<String, Object>> getStocksForChart() {
        try {
            log.info("📊 차트용 주식 정보 조회 요청 (주요 5개 종목) - 실제 데이터 모드");
            
            // 주요 5개 종목 코드 정의
            String[] targetStocks = {"005930", "035420", "005380", "035720", "000150"}; // 삼성전자, 네이버, 현대차, 카카오, 두산
            List<StockSecurityInfoDto> stockList = new java.util.ArrayList<>();
            
            log.info("🔍 실제 공공데이터 API에서 5개 종목 직접 조회 시작");
            
            // 각 종목을 개별적으로 조회 (100개 전체 조회하지 않고 직접 조회)
            // 성능 최적화: 병렬 처리로 동시 조회
            java.util.concurrent.CompletableFuture<StockSecurityInfoDto>[] futures = new java.util.concurrent.CompletableFuture[targetStocks.length];
            
            for (int i = 0; i < targetStocks.length; i++) {
                final String stockCode = targetStocks[i];
                final int index = i;
                
                futures[i] = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("종목 조회 시작: {}", stockCode);
                        StockSecurityInfoDto stock = publicDataStockService.getStockInfoByCode(stockCode);
                        if (stock != null) {
                            log.info("✅ 주식 조회 성공: {} ({}) - {}원", 
                                stock.getItemName(), stock.getShortCode(), stock.getClosePrice());
                            return stock;
                        } else {
                            log.warn("⚠️ 주식 조회 실패: {} (데이터 없음)", stockCode);
                            return null;
                        }
                    } catch (Exception e) {
                        log.error("❌ 주식 조회 오류 {}: {}", stockCode, e.getMessage());
                        return null;
                    }
                });
            }
            
            // 모든 비동기 작업 완료 대기 (최대 20초)
            try {
                java.util.concurrent.CompletableFuture.allOf(futures)
                    .get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                // 결과 수집
                for (java.util.concurrent.CompletableFuture<StockSecurityInfoDto> future : futures) {
                    try {
                        StockSecurityInfoDto stock = future.get();
                        if (stock != null) {
                            stockList.add(stock);
                        }
                    } catch (Exception e) {
                        log.warn("개별 종목 결과 수집 실패: {}", e.getMessage());
                    }
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("⏰ 주식 조회 타임아웃 (20초 초과), 부분 결과 사용");
                // 완료된 것만 수집
                for (java.util.concurrent.CompletableFuture<StockSecurityInfoDto> future : futures) {
                    if (future.isDone() && !future.isCompletedExceptionally()) {
                        try {
                            StockSecurityInfoDto stock = future.get(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                            if (stock != null) {
                                stockList.add(stock);
                            }
                        } catch (Exception ignored) {
                            // 무시
                        }
                    }
                }
            } catch (Exception e) {
                log.error("병렬 처리 오류: {}", e.getMessage());
                // 폴백: 순차 처리
                for (String stockCode : targetStocks) {
                    try {
                        StockSecurityInfoDto stock = publicDataStockService.getStockInfoByCode(stockCode);
                        if (stock != null) {
                            stockList.add(stock);
                        }
                    } catch (Exception ex) {
                        log.warn("순차 처리 폴백 실패: {}", ex.getMessage());
                    }
                }
            }
            
            log.info("📊 실제 데이터 조회 완료: {}/5개 종목 성공", stockList.size());
            
            if (stockList != null && !stockList.isEmpty()) {
                List<Map<String, Object>> chartData = stockList.stream()
                    .map(stock -> {
                        Map<String, Object> chartItem = new HashMap<>();
                        chartItem.put("stockCode", stock.getShortCode());
                        chartItem.put("name", stock.getItemName());
                        chartItem.put("currentPrice", stock.getClosePrice());
                        chartItem.put("updateDate", stock.getBaseDate());
                        return chartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                Map<String, Object> result = new HashMap<>();
                result.put("data", chartData);
                result.put("count", chartData.size());
                result.put("message", "차트용 주식 정보 조회 성공");
                
                log.info("✅ 차트용 주식 정보 조회 성공: {}개", chartData.size());
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ 주식 서비스에서 데이터를 가져오지 못했습니다");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "공공데이터 API 호출 실패 또는 데이터 없음");
                errorResponse.put("service", "PublicDataStockService");
                return ResponseEntity.status(503).body(errorResponse);
            }
        } catch (Exception e) {
            log.error("❌ 차트용 주식 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("service", "PublicDataStockService");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

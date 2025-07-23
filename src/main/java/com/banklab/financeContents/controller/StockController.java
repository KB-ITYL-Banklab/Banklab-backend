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
    @ApiOperation(value = "웹페이지 차트용 주식 정보 조회")
    public ResponseEntity<Map<String, Object>> getStocksForChart() {
        try {
            log.info("📊 차트용 주식 정보 조회 요청");
            
            List<StockSecurityInfoDto> stockList = publicDataStockService.getStockPriceInfo(
                    null, null, 20, 1);
            
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "최근 영업일 데이터를 찾을 수 없습니다");
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 차트용 주식 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

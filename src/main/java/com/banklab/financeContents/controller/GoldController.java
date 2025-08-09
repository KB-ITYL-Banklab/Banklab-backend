package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.GoldPriceInfoDto;
import com.banklab.financeContents.service.GoldPriceService;
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
 * 금 시세 정보 조회 REST API 컨트롤러
 * 
 * 이 컨트롤러는 공공데이터포털의 일반상품시세정보 API를 통해 
 * KRX 금 시장의 시세 관련 정보를 조회하는 REST API 엔드포인트들을 제공합니다.
 * 
 * 주요 기능:
 * - 최신 금 시세 조회
 * - 특정 금 상품 정보 조회 (상품코드 기반)
 * - 금 시세 목록 조회 (페이징 지원)
 * - 특정 날짜의 금 시세 조회
 *
 * API 문서: Swagger UI에서 확인 가능
 * 기본 경로: /api/gold
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2025.01
 * @see GoldPriceService 실제 비즈니스 로직 처리
 * @see GoldPriceInfoDto 금 시세 정보 데이터 구조
 */
@Slf4j
@RestController
@RequestMapping("/api/gold")
@Api(tags = "금 시세 정보 API", description = "현재 금 시세 정보를 불러옴")
public class GoldController {
    
    /** 금 시세 서비스 (스프링 의존성 주입) */
    @Autowired
    private GoldPriceService goldPriceService;

    
    /**
     * 금 시세 요약 정보 조회 엔드포인트
     * 
     * 금 시세의 간단한 요약 정보를 제공합니다.
     * 
     * @return ResponseEntity 금 시세 요약 정보
     */
    @GetMapping("/chart")
    @ApiOperation(value = "웹페이지 차트용 금 시세 정보 조회",  notes = "최신 금 시세 10개를 차트 형식으로 조회.")
    public ResponseEntity<Map<String, Object>> getGoldForChart() {
        try {
            log.info("📊 차트용 금 시세 정보 조회 요청");
            
            List<GoldPriceInfoDto> goldList = goldPriceService.getLatestGoldPrices(10);
            log.info("금 서비스 조회 결과: {}", goldList != null ? goldList.size() : "null");
            
            if (goldList != null && !goldList.isEmpty()) {
                List<Map<String, Object>> chartData = goldList.stream()
                    .map(gold -> {
                        Map<String, Object> chartItem = new HashMap<>();
                        chartItem.put("name", gold.getItemName());
                        chartItem.put("currentPrice", gold.getClosePrice());
                        chartItem.put("updateDate", gold.getBaseDate());
                        return chartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                Map<String, Object> result = new HashMap<>();
                result.put("data", chartData);
                result.put("count", chartData.size());
                result.put("message", "차트용 금 시세 정보 조회 성공");
                
                log.info("✅ 차트용 금 시세 정보 조회 성공: {}개", chartData.size());
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ 금 시세 서비스에서 데이터를 가져오지 못했습니다");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "공공데이터 API 호출 실패 또는 데이터 없음");
                errorResponse.put("service", "GoldPriceService");
                return ResponseEntity.status(503).body(errorResponse);
            }
        } catch (Exception e) {
            log.error("❌ 차트용 금 시세 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("service", "GoldPriceService");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
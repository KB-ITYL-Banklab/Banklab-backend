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
@Api(tags = "금 시세 정보 API")
public class GoldController {
    
    /** 금 시세 서비스 (스프링 의존성 주입) */
    @Autowired
    private GoldPriceService goldPriceService;

    /**
     * 최신 금 시세 조회 엔드포인트
     * 
     * 가장 최근 영업일의 금 시세 정보를 조회합니다.
     * 데이터가 없으면 최대 7일 전까지 자동으로 탐색합니다.
     * 
     * @param count 조회할 금 상품 수 (기본값: 10, 최대값: 100)
     * @return ResponseEntity 최신 금 시세 정보 또는 오류 메시지
     */
    @GetMapping("/latest")
    @ApiOperation(value = "최신 금 시세 조회", notes = "가장 최근 영업일의 금 시세 정보를 조회합니다.")
    public ResponseEntity<?> getLatestGoldPrices(
            @ApiParam(value = "조회할 금 상품 수 (기본: 10, 최대: 100)", example = "10")
            @RequestParam(defaultValue = "10") int count) {
        try {
            log.info("🏆 최신 금 시세 {} 조회 요청", count);
            
            // 파라미터 검증
            if (count <= 0 || count > 100) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회 개수는 1~100 사이여야 합니다");
                errorResponse.put("requestedCount", count);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            List<GoldPriceInfoDto> goldPrices = goldPriceService.getLatestGoldPrices(count);
            
            if (goldPrices != null && !goldPrices.isEmpty()) {
                log.info("✅ 최신 금 시세 조회 성공: {}개", goldPrices.size());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("data", goldPrices);
                successResponse.put("count", goldPrices.size());
                successResponse.put("requestedCount", count);
                successResponse.put("message", "최신 금 시세 조회 성공");
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "최근 7일간 금 시세 데이터를 찾을 수 없습니다");
                errorResponse.put("requestedCount", count);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 최신 금 시세 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestedCount", count);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 특정 금 상품 정보 조회 엔드포인트
     * 
     * 상품코드를 이용하여 특정 금 상품의 시세 정보를 조회합니다.
     * 
     * @param productCode 금 상품코드 (필수)
     * @return ResponseEntity 해당 금 상품 정보 또는 오류 메시지
     */
    @GetMapping("/product/{productCode}")
    @ApiOperation(value = "특정 금 상품 정보 조회", notes = "상품코드를 이용하여 특정 금 상품의 시세 정보를 조회합니다.")
    public ResponseEntity<?> getGoldPriceByProductCode(
            @ApiParam(value = "금 상품코드", required = true, example = "KRX_GOLD_001")
            @PathVariable String productCode) {
        try {
            log.info("🔍 금 상품 {} 조회 요청", productCode);
            
            // 상품코드 검증
            if (productCode == null || productCode.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "상품코드는 필수입니다");
                errorResponse.put("requestedCode", productCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            GoldPriceInfoDto goldInfo = goldPriceService.getGoldPriceByProductCode(productCode.trim());
            if (goldInfo != null) {
                log.info("✅ 금 상품 {} 조회 성공: {}", productCode, goldInfo.getProductName());
                return ResponseEntity.ok(goldInfo);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "해당 금 상품을 찾을 수 없습니다");
                errorResponse.put("requestedCode", productCode);
                errorResponse.put("message", "최근 7일간의 데이터에서 해당 상품을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ 금 상품 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestedCode", productCode);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 금 시세 목록 조회 엔드포인트 (페이징 지원)
     * 
     * 특정 날짜의 금 시세 목록을 페이징을 통해 조회합니다.
     * 
     * @param baseDate 기준일자 (YYYYMMDD 형식, 선택사항 - 미입력시 전일)
     * @param numOfRows 조회할 데이터 수 (기본값: 10, 최대값: 1000)
     * @param pageNo 페이지 번호 (기본값: 1)
     * @return ResponseEntity 금 시세 목록 또는 오류 메시지
     */
    @GetMapping("/list")
    @ApiOperation(value = "금 시세 목록 조회", notes = "특정 날짜의 금 시세 목록을 페이징을 통해 조회합니다.")
    public ResponseEntity<?> getGoldPriceList(
            @ApiParam(value = "기준일자 (YYYYMMDD), 미입력시 전일", example = "20250122")
            @RequestParam(required = false) String baseDate,
            
            @ApiParam(value = "조회할 데이터 수 (기본: 10, 최대: 1000)", example = "10")
            @RequestParam(defaultValue = "10") int numOfRows,
            
            @ApiParam(value = "페이지 번호 (기본: 1)", example = "1")
            @RequestParam(defaultValue = "1") int pageNo) {
        try {
            log.info("📊 금 시세 목록 조회 요청 - 기준일:{}, 개수:{}, 페이지:{}", baseDate, numOfRows, pageNo);
            
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
            
            List<GoldPriceInfoDto> goldList = goldPriceService.getGoldPriceInfo(
                    baseDate, null, numOfRows, pageNo);
            
            if (goldList != null && !goldList.isEmpty()) {
                log.info("✅ 금 시세 목록 조회 성공: {}개", goldList.size());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("data", goldList);
                successResponse.put("count", goldList.size());
                successResponse.put("page", pageNo);
                successResponse.put("numOfRows", numOfRows);
                successResponse.put("baseDate", baseDate);
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "조회된 데이터가 없습니다");
                errorResponse.put("baseDate", baseDate);
                errorResponse.put("page", pageNo);
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ 금 시세 목록 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 금 시세 요약 정보 조회 엔드포인트
     * 
     * 금 시세의 간단한 요약 정보를 제공합니다.
     * 
     * @return ResponseEntity 금 시세 요약 정보
     */
    @GetMapping("/summary")
    @ApiOperation(value = "금 시세 요약 정보 조회", notes = "금 시세의 간단한 요약 정보를 제공합니다.")
    public ResponseEntity<Map<String, Object>> getGoldSummary() {
        try {
            log.info("📈 금 시세 요약 정보 조회 요청");
            
            // 최신 금 시세 3개 조회
            List<GoldPriceInfoDto> latestGoldPrices = goldPriceService.getLatestGoldPrices(3);
            
            Map<String, Object> result = new HashMap<>();
            if (latestGoldPrices != null && !latestGoldPrices.isEmpty()) {
                result.put("latestPrices", latestGoldPrices);
                result.put("count", latestGoldPrices.size());
                result.put("message", "금 시세 요약 정보 조회 성공");
                result.put("lastUpdated", latestGoldPrices.get(0).getBaseDate());
            } else {
                result.put("message", "최근 금 시세 데이터가 없습니다");
                result.put("count", 0);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 금 시세 요약 정보 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 특정 날짜의 금 시세 조회 엔드포인트
     * 
     * 기본 금 시세 조회 엔드포인트 - 다른 엔드포인트들의 기본 기능
     * 
     * @param baseDate 기준일자 (YYYYMMDD 형식)
     * @return ResponseEntity 해당 날짜의 금 시세 정보
     */
    @GetMapping("/{baseDate}")
    @ApiOperation(value = "특정 날짜 금 시세 조회", notes = "지정된 날짜의 금 시세 정보를 조회합니다.")
    public ResponseEntity<?> getGoldPriceByDate(
            @ApiParam(value = "조회할 날짜 (YYYYMMDD)", required = true, example = "20250122")
            @PathVariable String baseDate) {
        try {
            log.info("📅 {} 금 시세 조회 요청", baseDate);
            
            // 날짜 형식 간단 검증
            if (baseDate == null || baseDate.length() != 8) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "날짜는 YYYYMMDD 형식이어야 합니다");
                errorResponse.put("requestedDate", baseDate);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            List<GoldPriceInfoDto> goldPrices = goldPriceService.getGoldPriceInfo(baseDate, null, 10, 1);
            
            if (goldPrices != null && !goldPrices.isEmpty()) {
                log.info("✅ {} 금 시세 조회 성공: {}개", baseDate, goldPrices.size());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("data", goldPrices);
                successResponse.put("count", goldPrices.size());
                successResponse.put("baseDate", baseDate);
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "해당 날짜의 데이터가 없습니다");
                errorResponse.put("requestedDate", baseDate);
                errorResponse.put("message", "주말이나 공휴일에는 거래 데이터가 없을 수 있습니다");
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("❌ {} 금 시세 조회 실패: {}", baseDate, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestedDate", baseDate);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
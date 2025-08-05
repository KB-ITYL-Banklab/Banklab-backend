package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.UpbitCryptoDTO;
import com.banklab.financeContents.service.UpbitCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 업비트 가상화폐 시세 정보를 DB에 저장하고 조회하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/upbit/crypto")
@Api(tags = "업비트 가상화폐 DB 관리 API")
public class UpbitCryptoController {

    private static final Logger logger = LoggerFactory.getLogger(UpbitCryptoController.class);

    @Autowired
    private UpbitCryptoService upbitCryptoService;

    /**
     * 업비트 API에서 가상화폐 시세를 가져와서 DB에 저장
     */
    @PostMapping("/fetch-and-save")
    @ApiOperation(value = "가상화폐 시세 데이터 가져와서 저장", notes = "업비트 API에서 지정된 마켓들의 시세 정보를 가져와서 데이터베이스에 저장합니다.")
    public ResponseEntity<Map<String, Object>> fetchAndSaveCryptoData(
            @ApiParam(value = "마켓 코드들 (쉼표로 구분)", example = "KRW-BTC,KRW-ETH,KRW-XRP") 
            @RequestParam(defaultValue = "KRW-BTC,KRW-ETH,KRW-XRP,KRW-ADA,KRW-DOT") String markets) {
        
        try {
            logger.info("가상화폐 시세 데이터 저장 요청: {}", markets);

            int savedCount = upbitCryptoService.fetchAndSaveCryptoData(markets);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "가상화폐 시세 데이터 저장 완료");
            response.put("savedCount", savedCount);
            response.put("markets", markets);

            logger.info("가상화폐 시세 데이터 저장 성공: {}개", savedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("가상화폐 시세 데이터 저장 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "가상화폐 시세 데이터 저장 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 인기 가상화폐들의 시세를 가져와서 저장
     */
    @PostMapping("/fetch-popular")
    @ApiOperation(value = "인기 가상화폐 시세 저장", notes = "비트코인, 이더리움 등 인기 가상화폐들의 시세를 가져와서 저장합니다.")
    public ResponseEntity<Map<String, Object>> fetchAndSavePopularCryptos() {
        try {
            logger.info("인기 가상화폐 시세 데이터 저장 요청");

            int savedCount = upbitCryptoService.fetchAndSavePopularCryptos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인기 가상화폐 시세 데이터 저장 완료");
            response.put("savedCount", savedCount);

            logger.info("인기 가상화폐 시세 데이터 저장 성공: {}개", savedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("인기 가상화폐 시세 데이터 저장 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인기 가상화폐 시세 데이터 저장 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 특정 마켓의 최신 시세 정보 조회
     */
    @GetMapping("/latest/{market}")
    @ApiOperation(value = "특정 마켓 최신 시세 조회", notes = "DB에서 특정 마켓의 가장 최신 시세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getLatestByMarket(
            @ApiParam(value = "마켓 코드", example = "KRW-BTC") 
            @PathVariable String market) {
        
        try {
            logger.debug("{}의 최신 시세 정보 조회 요청", market);

            UpbitCryptoDTO cryptoData = upbitCryptoService.getLatestByMarket(market);

            Map<String, Object> response = new HashMap<>();
            if (cryptoData != null) {
                response.put("success", true);
                response.put("data", cryptoData);
                response.put("message", market + "의 최신 시세 정보 조회 성공");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", market + "의 시세 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("최신 시세 정보 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "최신 시세 정보 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 모든 가상화폐의 최신 시세 정보 조회
     */
    @GetMapping("/latest/all")
    @ApiOperation(value = "모든 가상화폐 최신 시세 조회", notes = "DB에서 모든 가상화폐의 최신 시세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getAllLatest() {
        try {
            logger.debug("모든 가상화폐의 최신 시세 정보 조회 요청");

            List<UpbitCryptoDTO> cryptoList = upbitCryptoService.getAllLatest();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cryptoList);
            response.put("count", cryptoList.size());
            response.put("message", "모든 가상화폐 최신 시세 정보 조회 성공");

            logger.debug("모든 가상화폐 최신 시세 정보 조회 성공: {}개", cryptoList.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("모든 최신 시세 정보 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "모든 최신 시세 정보 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 특정 마켓의 시세 정보 페이징 조회
     */
    @GetMapping("/{market}")
    @ApiOperation(value = "특정 마켓 시세 이력 조회", notes = "DB에서 특정 마켓의 시세 이력을 페이징으로 조회합니다.")
    public ResponseEntity<Map<String, Object>> getCryptoByMarket(
            @ApiParam(value = "마켓 코드", example = "KRW-BTC") 
            @PathVariable String market,
            @ApiParam(value = "페이지 번호 (0부터 시작)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            logger.debug("{}의 시세 이력 조회 요청: {}페이지, {}개", market, page, size);

            List<UpbitCryptoDTO> cryptoList = upbitCryptoService.getCryptoByMarket(market, page, size);
            int totalCount = upbitCryptoService.getCountByMarket(market);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cryptoList);
            response.put("totalCount", totalCount);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("message", market + "의 시세 이력 조회 성공");

            logger.debug("{}의 시세 이력 조회 성공: {}개", market, cryptoList.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("시세 이력 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "시세 이력 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * ID로 특정 시세 정보 조회
     */
    @GetMapping("/id/{id}")
    @ApiOperation(value = "ID로 시세 정보 조회", notes = "고유 ID로 특정 시세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getCryptoById(
            @ApiParam(value = "고유 ID", example = "1") 
            @PathVariable Long id) {
        
        try {
            logger.debug("ID {}의 시세 정보 조회 요청", id);

            UpbitCryptoDTO cryptoData = upbitCryptoService.getCryptoById(id);

            Map<String, Object> response = new HashMap<>();
            if (cryptoData != null) {
                response.put("success", true);
                response.put("data", cryptoData);
                response.put("message", "시세 정보 조회 성공");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "해당 ID의 시세 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("ID로 시세 정보 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "시세 정보 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 통계 정보 조회
     */
    @GetMapping("/stats")
    @ApiOperation(value = "시세 데이터 통계 조회", notes = "DB에 저장된 시세 데이터의 통계 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            logger.debug("시세 데이터 통계 정보 조회 요청");

            int totalCount = upbitCryptoService.getTotalCount();
            List<UpbitCryptoDTO> allLatest = upbitCryptoService.getAllLatest();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalRecords", totalCount);
            response.put("uniqueMarkets", allLatest.size());
            response.put("latestData", allLatest);
            response.put("message", "통계 정보 조회 성공");

            logger.debug("통계 정보 조회 성공: 총 {}개 레코드, {}개 마켓", totalCount, allLatest.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("통계 정보 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "통계 정보 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 오래된 데이터 정리
     */
    @DeleteMapping("/cleanup")
    @ApiOperation(value = "오래된 데이터 정리", notes = "지정된 일수 이전의 오래된 시세 데이터를 삭제합니다.")
    public ResponseEntity<Map<String, Object>> cleanupOldData(
            @ApiParam(value = "보관할 일수", example = "30") 
            @RequestParam(defaultValue = "30") int daysToKeep) {
        
        try {
            logger.info("오래된 데이터 정리 요청: {}일 이전 데이터 삭제", daysToKeep);

            int deletedCount = upbitCryptoService.cleanOldData(daysToKeep);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("daysToKeep", daysToKeep);
            response.put("message", "오래된 데이터 정리 완료");

            logger.info("오래된 데이터 정리 성공: {}개 삭제", deletedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("오래된 데이터 정리 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "오래된 데이터 정리 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 날짜 범위로 데이터 조회
     */
    @GetMapping("/date-range")
    @ApiOperation(value = "날짜 범위로 시세 데이터 조회", notes = "지정된 날짜 범위의 시세 데이터를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getCryptoByDateRange(
            @ApiParam(value = "시작 날짜", example = "2024-01-01") 
            @RequestParam String startDate,
            @ApiParam(value = "종료 날짜", example = "2024-01-31") 
            @RequestParam String endDate,
            @ApiParam(value = "마켓 코드 (선택)", example = "KRW-BTC") 
            @RequestParam(required = false) String market,
            @ApiParam(value = "페이지 번호", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            logger.debug("날짜 범위 조회 요청: {} ~ {}, 마켓: {}", startDate, endDate, market);

            List<UpbitCryptoDTO> cryptoList = upbitCryptoService.getCryptoByDateRange(startDate, endDate, market, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cryptoList);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("market", market);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("count", cryptoList.size());
            response.put("message", "날짜 범위 조회 성공");

            logger.debug("날짜 범위 조회 성공: {}개", cryptoList.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("날짜 범위 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "날짜 범위 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 오늘 데이터 조회
     */
    @GetMapping("/today")
    @ApiOperation(value = "오늘 시세 데이터 조회", notes = "오늘 저장된 시세 데이터를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getTodayData(
            @ApiParam(value = "마켓 코드 (선택)", example = "KRW-BTC") 
            @RequestParam(required = false) String market) {
        
        try {
            logger.debug("오늘 데이터 조회 요청: 마켓 {}", market);

            List<UpbitCryptoDTO> cryptoList = upbitCryptoService.getTodayData(market);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cryptoList);
            response.put("market", market);
            response.put("count", cryptoList.size());
            response.put("message", "오늘 데이터 조회 성공");

            logger.debug("오늘 데이터 조회 성공: {}개", cryptoList.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("오늘 데이터 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "오늘 데이터 조회 실패");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

package com.banklab.financeContents.controller;

import com.banklab.financeContents.domain.FinanceUpbit;
import com.banklab.financeContents.scheduler.UpbitDataScheduler;
import com.banklab.financeContents.service.UpbitDataService;
import com.banklab.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 업비트 데이터 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/upbit")
@RequiredArgsConstructor
public class UpbitController {

    private final UpbitDataService upbitDataService;
    private final UpbitDataScheduler upbitDataScheduler;

    /**
     * 업비트 데이터 수동 수집
     */
    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<String>> collectUpbitData() {
        try {
            upbitDataScheduler.manualCollectUpbitData();
            return ResponseEntity.ok(ApiResponse.success("업비트 데이터 수집이 완료되었습니다."));
        } catch (Exception e) {
            log.error("업비트 데이터 수집 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("업비트 데이터 수집에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 마켓의 최신 데이터 조회
     */
    @GetMapping("/latest/{market}")
    public ResponseEntity<ApiResponse<FinanceUpbit>> getLatestDataByMarket(@PathVariable String market) {
        try {
            FinanceUpbit data = upbitDataService.getLatestDataByMarket(market);
            
            if (data != null) {
                return ResponseEntity.ok(ApiResponse.success(data));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "해당 마켓의 데이터가 없습니다."));
            }
        } catch (Exception e) {
            log.error("업비트 데이터 조회 실패: {}", market, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 마켓의 최신 데이터 조회
     */
    @GetMapping("/latest/all")
    public ResponseEntity<ApiResponse<List<FinanceUpbit>>> getAllLatestData() {
        try {
            List<FinanceUpbit> dataList = upbitDataService.getAllLatestData();
            return ResponseEntity.ok(ApiResponse.success(dataList));
        } catch (Exception e) {
            log.error("모든 업비트 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    /**
     * 인기 코인 top 5 조회 (비트코인, 이더리움 등)
     */
    @GetMapping("/top-coins")
    public ResponseEntity<ApiResponse<List<FinanceUpbit>>> getTopCoins() {
        try {
            List<FinanceUpbit> allData = upbitDataService.getAllLatestData();
            
            // 주요 코인들만 필터링
            List<String> topCoins = List.of("KRW-BTC", "KRW-ETH", "KRW-XRP", "KRW-ADA", "KRW-DOT");
            List<FinanceUpbit> topCoinData = allData.stream()
                .filter(data -> topCoins.contains(data.getMarket()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(topCoinData, "주요 코인 데이터"));
        } catch (Exception e) {
            log.error("주요 코인 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("주요 코인 데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 최근 한달치 실제 과거 데이터를 DB table에 insert 하는 엔드포인트
     * 업비트 API의 일봉 캔들 데이터를 사용하여 실제 과거 한달치 데이터를 수집하고 저장합니다.
     */
    @PostMapping("/insert-monthly")
    public ResponseEntity<ApiResponse<String>> insertMonthlyData() {
        try {
            log.info("최근 한달치 실제 데이터 삽입 요청");
            upbitDataService.insertMonthlyData();
            return ResponseEntity.ok(ApiResponse.success("최근 한달치 실제 데이터 삽입이 완료되었습니다."));
        } catch (Exception e) {
            log.error("최근 한달치 데이터 삽입 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("최근 한달치 데이터 삽입에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 종목명(마켓코드)으로 검색하면 해당 종목의 데이터들을 DB에서 조회하는 엔드포인트
     */
    @GetMapping("/search/{market}")
    public ResponseEntity<ApiResponse<List<FinanceUpbit>>> searchByMarket(@PathVariable String market) {
        try {
            log.info("종목 검색 요청: {}", market);
            
            // 마켓코드 형식 검증 (선택사항)
            if (market == null || market.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("종목명(마켓코드)을 입력해주세요."));
            }
            
            // 대문자로 변환 (업비트 마켓코드는 대문자)
            String upperMarket = market.toUpperCase();
            
            List<FinanceUpbit> dataList = upbitDataService.getDataByMarket(upperMarket);
            
            if (dataList.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(dataList, 
                    "해당 종목(" + upperMarket + ")의 데이터가 없습니다."));
            }
            
            return ResponseEntity.ok(ApiResponse.success(dataList, 
                "종목 " + upperMarket + "의 데이터 " + dataList.size() + "건을 조회했습니다."));
                
        } catch (Exception e) {
            log.error("종목 검색 실패: {}", market, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("종목 검색에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 종목명(마켓코드)과 기간으로 검색하는 엔드포인트 (추가 기능)
     */
    @GetMapping("/search/{market}/period")
    public ResponseEntity<ApiResponse<List<FinanceUpbit>>> searchByMarketAndPeriod(
            @PathVariable String market,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("종목 기간별 검색 요청: {}, {} ~ {}", market, startDate, endDate);
            
            // 마켓코드 형식 검증
            if (market == null || market.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("종목명(마켓코드)을 입력해주세요."));
            }
            
            String upperMarket = market.toUpperCase();
            
            List<FinanceUpbit> dataList;
            
            if (startDate != null && endDate != null) {
                // 기간이 지정된 경우
                dataList = upbitDataService.getDataByMarketAndDateRange(upperMarket, startDate, endDate);
            } else {
                // 기간이 지정되지 않은 경우 전체 데이터 조회
                dataList = upbitDataService.getDataByMarket(upperMarket);
            }
            
            if (dataList.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(dataList, 
                    "해당 조건의 데이터가 없습니다."));
            }
            
            String message = String.format("종목 %s의 데이터 %d건을 조회했습니다.", 
                upperMarket, dataList.size());
            if (startDate != null && endDate != null) {
                message += String.format(" (기간: %s ~ %s)", startDate, endDate);
            }
            
            return ResponseEntity.ok(ApiResponse.success(dataList, message));
                
        } catch (Exception e) {
            log.error("종목 기간별 검색 실패: {}", market, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("종목 기간별 검색에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 실시간 1분봉 데이터 조회 (단일 종목)
     * Insert 없이 업비트 API에서 직접 가져와서 보여주는 실시간 데이터
     */
    @GetMapping("/realtime/{market}")
    public ResponseEntity<ApiResponse<com.banklab.financeContents.dto.RealtimeDataDto>> getRealtimeData(@PathVariable String market) {
        try {
            log.info("실시간 데이터 조회 요청: {}", market);
            
            // 마켓코드 형식 검증
            if (market == null || market.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("종목명(마켓코드)을 입력해주세요."));
            }
            
            String upperMarket = market.toUpperCase();
            
            FinanceUpbit realtimeData = upbitDataService.getRealtimeCandle(upperMarket);
            
            if (realtimeData == null) {
                return ResponseEntity.ok(ApiResponse.success(null, 
                    "해당 종목(" + upperMarket + ")의 실시간 데이터를 가져올 수 없습니다. " +
                    "존재하는 마켓인지 확인해주세요. 예: KRW-BTC, KRW-ETH, KRW-XRP"));
            }
            
            // RealtimeDataDto로 변환 (불필요한 필드 제거)
            com.banklab.financeContents.dto.RealtimeDataDto responseDto = 
                com.banklab.financeContents.dto.RealtimeDataDto.from(realtimeData);
            
            String message = String.format("종목 %s의 실시간 1분봉 데이터입니다.", upperMarket);
            
            return ResponseEntity.ok(ApiResponse.success(responseDto, message));
                
        } catch (Exception e) {
            log.error("실시간 데이터 조회 실패: {}", market, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("실시간 데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 KRW 마켓의 실시간 1분봉 데이터 조회
     * Insert 없이 업비트 API에서 직접 가져와서 보여주는 실시간 데이터
     */
    @GetMapping("/realtime/all")
    public ResponseEntity<ApiResponse<List<com.banklab.financeContents.dto.RealtimeDataDto>>> getAllRealtimeData() {
        try {
            log.info("모든 마켓 실시간 데이터 조회 요청");
            
            List<FinanceUpbit> realtimeDataList = upbitDataService.getAllRealtimeCandles();
            
            if (realtimeDataList.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(List.of(), 
                    "실시간 데이터를 가져올 수 없습니다."));
            }
            
            // RealtimeDataDto로 변환
            List<com.banklab.financeContents.dto.RealtimeDataDto> responseDtoList = realtimeDataList.stream()
                .map(com.banklab.financeContents.dto.RealtimeDataDto::from)
                .toList();
            
            String message = String.format("모든 KRW 마켓의 실시간 1분봉 데이터 %d건입니다.", 
                responseDtoList.size());
            
            return ResponseEntity.ok(ApiResponse.success(responseDtoList, message));
                
        } catch (Exception e) {
            log.error("모든 마켓 실시간 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("실시간 데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}

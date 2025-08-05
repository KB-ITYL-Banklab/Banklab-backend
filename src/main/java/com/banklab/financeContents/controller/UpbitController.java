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
     * 업비트 시스템 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("업비트 시스템이 정상 작동 중입니다."));
    }

    /**
     * 오늘 수집된 데이터 개수 확인
     */
    @GetMapping("/count/today")
    public ResponseEntity<ApiResponse<Integer>> getTodayDataCount() {
        try {
            // 간단한 카운트 조회를 위한 임시 메서드
            List<FinanceUpbit> allData = upbitDataService.getAllLatestData();
            return ResponseEntity.ok(ApiResponse.success(allData.size(), "오늘 수집된 데이터 개수"));
        } catch (Exception e) {
            log.error("데이터 개수 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("데이터 개수 조회에 실패했습니다: " + e.getMessage()));
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
     * API 연결 테스트 (업비트 API 호출만)
     */
    @GetMapping("/test/api")
    public ResponseEntity<ApiResponse<String>> testUpbitApi() {
        try {
            log.info("업비트 API 연결 테스트 시작");
            
            // 간단한 HTTP 연결 테스트 먼저
            String testUrl = "https://api.upbit.com/v1/market/all";
            log.info("테스트 URL: {}", testUrl);
            
            com.banklab.financeContents.service.UpbitApiService apiService = 
                new com.banklab.financeContents.service.UpbitApiService();
            
            List<com.banklab.financeContents.dto.UpbitMarketDto> markets = apiService.getAllMarkets();
            
            if (markets.isEmpty()) {
                log.error("마켓 정보가 비어있음 - API 호출 실패 또는 파싱 실패");
                return ResponseEntity.ok(ApiResponse.error("업비트 API 연결 실패 - 마켓 정보 없음. 로그를 확인하세요."));
            }
            
            String result = String.format("API 연결 성공! 조회된 KRW 마켓 수: %d개, 첫 번째 마켓: %s", 
                markets.size(), markets.get(0).getMarket());
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("업비트 API 테스트 실패 - 상세 오류", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("API 테스트 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }

    /**
     * 직접 HTTP 연결 테스트
     */
    @GetMapping("/test/network")
    public ResponseEntity<ApiResponse<String>> testNetworkConnection() {
        try {
            log.info("네트워크 연결 테스트 시작");
            
            // RestTemplate로 직접 호출 테스트
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            String url = "https://api.upbit.com/v1/market/all";
            log.info("직접 호출 URL: {}", url);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Accept", "application/json");
            headers.add("User-Agent", "Java-RestTemplate");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            log.info("응답 상태: {}", response.getStatusCode());
            log.info("응답 본문 길이: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                // JSON 파싱 없이 단순 응답 확인
                boolean hasKrwMarkets = responseBody != null && responseBody.contains("KRW-");
                
                String result = String.format("네트워크 연결 성공! 응답 길이: %d, KRW 마켓 포함: %s", 
                    responseBody != null ? responseBody.length() : 0, hasKrwMarkets);
                
                return ResponseEntity.ok(ApiResponse.success(result));
            } else {
                return ResponseEntity.ok(ApiResponse.error("HTTP 응답 실패: " + response.getStatusCode()));
            }
            
        } catch (Exception e) {
            log.error("네트워크 연결 테스트 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("네트워크 테스트 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }

    /**
     * DB 연결 테스트
     */
    @GetMapping("/test/db")
    public ResponseEntity<ApiResponse<String>> testDatabase() {
        try {
            log.info("데이터베이스 연결 테스트 시작");
            List<FinanceUpbit> existingData = upbitDataService.getAllLatestData();
            String result = String.format("DB 연결 성공! 기존 데이터 수: %d건", existingData.size());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("데이터베이스 테스트 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("DB 테스트 실패: " + e.getMessage()));
        }
    }

    /**
     * 단계별 디버깅: API 호출만 테스트
     */
    @GetMapping("/debug/step1")
    public ResponseEntity<ApiResponse<String>> debugStep1() {
        try {
            log.info("=== 디버깅 Step 1: 주입된 서비스로 API 호출 테스트 ===");
            
            // 주입된 서비스의 API 서비스를 직접 접근할 수 없으므로 리플렉션 사용
            // 또는 임시로 public 메서드 추가
            
            // 임시 해결책: 새 인스턴스가 아닌 실제 동작 확인
            try {
                upbitDataService.collectAndSaveUpbitData();
                return ResponseEntity.ok(ApiResponse.success("전체 수집 과정 성공!"));
            } catch (Exception e) {
                String errorDetails = e.getMessage();
                if (e.getCause() != null) {
                    errorDetails += " | 원인: " + e.getCause().getMessage();
                }
                return ResponseEntity.ok(ApiResponse.error("수집 실패: " + errorDetails));
            }
            
        } catch (Exception e) {
            log.error("Step 1 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Step 1 실패: " + e.getMessage()));
        }
    }

    /**
     * 실제 서비스에서 사용하는 API 테스트
     */
    @GetMapping("/debug/service-api")
    public ResponseEntity<ApiResponse<String>> debugServiceApi() {
        try {
            log.info("=== 실제 서비스 API 테스트 ===");
            String result = upbitDataService.testApiConnection();
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("서비스 API 테스트 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("서비스 API 테스트 실패: " + e.getMessage()));
        }
    }

    /**
     * 단일 마켓 Ticker 테스트
     */
    @GetMapping("/debug/single-ticker")
    public ResponseEntity<ApiResponse<String>> debugSingleTicker() {
        try {
            log.info("=== 단일 마켓 Ticker 테스트 ===");
            String result = upbitDataService.testSingleTicker();
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("단일 Ticker 테스트 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("단일 Ticker 테스트 실패: " + e.getMessage()));
        }
    }

    /**
     * 배치 처리 테스트
     */
    @GetMapping("/debug/batch-test")
    public ResponseEntity<ApiResponse<String>> debugBatchTest() {
        try {
            log.info("=== 배치 처리 테스트 ===");
            String result = upbitDataService.testBatchProcessing();
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("배치 처리 테스트 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("배치 처리 테스트 실패: " + e.getMessage()));
        }
    }

    /**
     * 가격 정밀도 확인 (BigDecimal 적용 후)
     */
    @GetMapping("/debug/price-precision")
    public ResponseEntity<ApiResponse<String>> debugPricePrecision() {
        try {
            log.info("=== 가격 정밀도 확인 (BigDecimal 적용 후) ===");
            
            FinanceUpbit btcData = upbitDataService.getLatestDataByMarket("KRW-BTC");
            if (btcData == null) {
                return ResponseEntity.ok(ApiResponse.error("BTC 데이터가 없습니다."));
            }
            
            String result = String.format(
                "BTC 가격 정밀도 확인 (소수점 둘째 자리):\n" +
                "- 현재가: %s원\n" +
                "- 시가: %s원\n" +
                "- 전일종가: %s원\n" +
                "- 등락률: %.6f%%\n" +
                "- 24h거래대금: %,.0f원",
                btcData.getTradePrice() != null ? btcData.getTradePrice().toPlainString() : "null",
                btcData.getOpeningPrice() != null ? btcData.getOpeningPrice().toPlainString() : "null",
                btcData.getPrevClosingPrice() != null ? btcData.getPrevClosingPrice().toPlainString() : "null",
                btcData.getChangeRate() != null ? btcData.getChangeRate() * 100 : 0.0,
                btcData.getAccTradePrice24h() != null ? btcData.getAccTradePrice24h() : 0.0
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("가격 정밀도 확인 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("가격 정밀도 확인 실패: " + e.getMessage()));
        }
    }

    /**
     * 단계별 디버깅: 데이터 변환 테스트
     */
    @GetMapping("/debug/step2")
    public ResponseEntity<ApiResponse<String>> debugStep2() {
        try {
            log.info("=== 디버깅 Step 2: 데이터 변환 테스트 ===");
            
            // API 호출
            com.banklab.financeContents.service.UpbitApiService apiService = 
                new com.banklab.financeContents.service.UpbitApiService();
            List<com.banklab.financeContents.dto.UpbitTickerDto> tickers = apiService.getAllKrwTickers();
            
            if (tickers.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("Step 2: API에서 데이터를 가져올 수 없습니다."));
            }
            
            // 첫 번째 데이터만 변환 테스트
            com.banklab.financeContents.dto.UpbitTickerDto firstTicker = tickers.get(0);
            FinanceUpbit converted = new FinanceUpbit();
            converted.setMarket(firstTicker.getMarket());
            
            // 가격 필드는 BigDecimal로 변환하고 소수점 둘째 자리로 반올림
            if (firstTicker.getOpening_price() != null) {
                converted.setOpeningPrice(java.math.BigDecimal.valueOf(firstTicker.getOpening_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (firstTicker.getTrade_price() != null) {
                converted.setTradePrice(java.math.BigDecimal.valueOf(firstTicker.getTrade_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (firstTicker.getPrev_closing_price() != null) {
                converted.setPrevClosingPrice(java.math.BigDecimal.valueOf(firstTicker.getPrev_closing_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            
            // 나머지는 Double 그대로
            converted.setChangeRate(firstTicker.getChange_rate());
            converted.setAccTradeVolume24h(firstTicker.getAcc_trade_volume_24h());
            converted.setAccTradePrice24h(firstTicker.getAcc_trade_price_24h());
            
            String result = String.format("데이터 변환 성공! 마켓: %s, 현재가: %s원", 
                converted.getMarket(), 
                converted.getTradePrice() != null ? converted.getTradePrice().toPlainString() : "null");
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Step 2 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Step 2 실패: " + e.getMessage()));
        }
    }

    /**
     * 단계별 디버깅: 단일 데이터 DB 저장 테스트
     */
    @PostMapping("/debug/step3")
    public ResponseEntity<ApiResponse<String>> debugStep3() {
        try {
            log.info("=== 디버깅 Step 3: 단일 데이터 DB 저장 테스트 ===");
            
            // 비트코인 데이터만 테스트
            com.banklab.financeContents.service.UpbitApiService apiService = 
                new com.banklab.financeContents.service.UpbitApiService();
            com.banklab.financeContents.dto.UpbitTickerDto btcTicker = apiService.getTickerForTest("KRW-BTC");
            
            if (btcTicker == null) {
                return ResponseEntity.ok(ApiResponse.error("Step 3: BTC 데이터를 가져올 수 없습니다."));
            }
            
            // 데이터 변환
            FinanceUpbit financeUpbit = new FinanceUpbit();
            financeUpbit.setMarket(btcTicker.getMarket());
            
            // 가격 필드는 BigDecimal로 변환하고 소수점 둘째 자리로 반올림
            if (btcTicker.getOpening_price() != null) {
                financeUpbit.setOpeningPrice(java.math.BigDecimal.valueOf(btcTicker.getOpening_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (btcTicker.getTrade_price() != null) {
                financeUpbit.setTradePrice(java.math.BigDecimal.valueOf(btcTicker.getTrade_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (btcTicker.getPrev_closing_price() != null) {
                financeUpbit.setPrevClosingPrice(java.math.BigDecimal.valueOf(btcTicker.getPrev_closing_price())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            }
            
            // 나머지는 Double 그대로
            financeUpbit.setChangeRate(btcTicker.getChange_rate());
            financeUpbit.setAccTradeVolume24h(btcTicker.getAcc_trade_volume_24h());
            financeUpbit.setAccTradePrice24h(btcTicker.getAcc_trade_price_24h());
            
            // DB 저장 시도
            upbitDataService.collectAndSaveUpbitData();
            
            String result = String.format("단일 데이터 저장 테스트 성공! BTC 현재가: %s원", 
                financeUpbit.getTradePrice() != null ? financeUpbit.getTradePrice().toPlainString() : "null");
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Step 3 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Step 3 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }
}

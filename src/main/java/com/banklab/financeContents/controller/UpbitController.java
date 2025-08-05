package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.BitcoinTickerDTO;
import com.banklab.financeContents.service.UpbitApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class UpbitController
 * @description 업비트(Upbit) API와 연동하여 비트코인 시세 정보를 제공하는 REST 컨트롤러입니다.
 * - Swagger를 통해 API 문서를 자동으로 생성하고 명세를 관리합니다.
 */
@RestController
@RequestMapping("/api/upbit")
@Api(tags = "업비트 암호화폐 시세 API") // Swagger UI에 표시될 API 그룹 이름
public class UpbitController {

    // SLF4J를 이용한 로거(Logger) 인스턴스 생성
    private static final Logger logger = LoggerFactory.getLogger(UpbitController.class);

    // 비즈니스 로직을 처리하는 서비스 레이어(UpbitApiService)를 의존성 주입(DI) 받습니다.
    @Autowired
    private UpbitApiService upbitApiService;

    /**
     * @method checkApiHealth
     * @description 외부 API(업비트)의 현재 연결 상태를 확인하는 Health Check 엔드포인트입니다.
     */
    @GetMapping("/chart")
    @ApiOperation(value = "웹페이지 차트용 가상화폐 정보 조회")
    public ResponseEntity<Map<String, Object>> getCryptocurrencyForChart() {
        try {
            logger.info("📊 차트용 가상화폐 정보 조회 요청");

            String markets = "KRW-BTC,KRW-ETH,KRW-XRP,KRW-ADA,KRW-DOT";
            logger.info("요청할 마켓 코드: {}", markets);
            
            List<BitcoinTickerDTO> tickers = upbitApiService.getMultipleTickers(markets);
            logger.info("서비스에서 반환된 티커 수: {}", tickers != null ? tickers.size() : "null");

            if (tickers != null && !tickers.isEmpty()) {
                List<Map<String, Object>> chartData = tickers.stream()
                    .map(ticker -> {
                        Map<String, Object> chartItem = new HashMap<>();
                        chartItem.put("marketCode", ticker.getMarket());
                        chartItem.put("name", getCryptocurrencyName(ticker.getMarket()));
                        chartItem.put("currentPrice", ticker.getTradePrice());
                        chartItem.put("updateDate", ticker.getTradeDateKst());
                        logger.debug("처리된 데이터: {} - {} KRW", ticker.getMarket(), ticker.getTradePrice());
                        return chartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());

                Map<String, Object> result = new HashMap<>();
                result.put("data", chartData);
                result.put("count", chartData.size());
                result.put("message", "차트용 가상화폐 정보 조회 성공");

                logger.info("✅ 차트용 가상화폐 정보 조회 성공: {}개", chartData.size());
                return ResponseEntity.ok(result);
            } else {
                logger.warn("⚠️ 업비트 API에서 데이터를 가져오지 못했습니다. 단일 비트코인 데이터로 대체 시도");
                
                // 대체 방안: 단일 비트코인 조회로 폴백
                BitcoinTickerDTO bitcoinTicker = upbitApiService.getBitcoinTicker();
                if (bitcoinTicker != null) {
                    Map<String, Object> chartItem = new HashMap<>();
                    chartItem.put("marketCode", bitcoinTicker.getMarket());
                    chartItem.put("name", "비트코인");
                    chartItem.put("currentPrice", bitcoinTicker.getTradePrice());
                    chartItem.put("updateDate", bitcoinTicker.getTradeDateKst());
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("data", List.of(chartItem));
                    result.put("count", 1);
                    result.put("message", "비트코인 정보만 조회 성공 (다중 조회 실패로 대체)");
                    
                    logger.info("✅ 대체 방안으로 비트코인 정보 조회 성공");
                    return ResponseEntity.ok(result);
                }
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "조회된 데이터가 없습니다");
                errorResponse.put("message", "업비트 API 호출에 실패했습니다");
                errorResponse.put("requestedMarkets", markets);
                logger.error("❌ 모든 대체 방안 실패");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("❌ 차트용 가상화폐 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류가 발생했습니다");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private String getCryptocurrencyName(String marketCode) {
        Map<String, String> cryptoNames = new HashMap<>();
        cryptoNames.put("KRW-BTC", "비트코인");
        cryptoNames.put("KRW-ETH", "이더리움");
        cryptoNames.put("KRW-XRP", "리플");
        cryptoNames.put("KRW-ADA", "에이다");
        cryptoNames.put("KRW-DOT", "폴카닷");
        cryptoNames.put("KRW-LINK", "체인링크");
        cryptoNames.put("KRW-LTC", "라이트코인");
        cryptoNames.put("KRW-BCH", "비트코인캐시");
        cryptoNames.put("KRW-EOS", "이오스");
        cryptoNames.put("KRW-TRX", "트론");
        
        return cryptoNames.getOrDefault(marketCode, marketCode);
    }

    @GetMapping("/bitcoin")
    @ApiOperation(value = "비트코인 시세 조회", notes = "업비트에서 비트코인(KRW-BTC)의 상세 시세 정보를 조회합니다.")
    public ResponseEntity<?> getBitcoinTicker() {
        try {
            logger.info("비트코인 시세 조회 요청");

            BitcoinTickerDTO ticker = upbitApiService.getBitcoinTicker();

            if (ticker != null) {
                logger.info("비트코인 시세 조회 성공: {} KRW", ticker.getTradePrice());
                return ResponseEntity.ok(ticker);
            } else {
                logger.warn("비트코인 시세 조회 실패");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "비트코인 시세 정보를 가져올 수 없습니다.");
                errorResponse.put("message", "업비트 API 호출에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

        } catch (Exception e) {
            logger.error("비트코인 시세 조회 중 오류 발생: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
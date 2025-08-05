package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.UpbitCandleDto;
import com.banklab.financeContents.dto.UpbitMarketDto;
import com.banklab.financeContents.dto.UpbitTickerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 업비트 API 호출 서비스
 */
@Slf4j
@Service
public class UpbitApiService {

    private static final String UPBIT_API_BASE_URL = "https://api.upbit.com/v1";
    private static final String MARKET_ALL_ENDPOINT = "/market/all";
    private static final String TICKER_ENDPOINT = "/ticker";
    private static final String CANDLES_DAYS_ENDPOINT = "/candles/days";
    private static final String CANDLES_MINUTES_ENDPOINT = "/candles/minutes";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UpbitApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 업비트 마켓 리스트 조회
     * @return 마켓 리스트
     */
    public List<UpbitMarketDto> getAllMarkets() {
        String url = UPBIT_API_BASE_URL + MARKET_ALL_ENDPOINT;
        log.info("업비트 마켓 리스트 API 호출 시작: {}", url);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            headers.add("User-Agent", "Java-RestTemplate");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("HTTP 요청 헤더: {}", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            log.info("업비트 마켓 API 응답 상태코드: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("API 응답 본문 길이: {} characters", responseBody != null ? responseBody.length() : 0);
                
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    log.error("응답 본문이 비어있음");
                    return List.of();
                }
                
                // 응답 본문 일부 로깅 (처음 200자)
                String preview = responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
                log.info("응답 본문 미리보기: {}", preview);
                
                try {
                    List<UpbitMarketDto> markets = objectMapper.readValue(
                        responseBody, new TypeReference<List<UpbitMarketDto>>() {});
                    
                    log.info("JSON 파싱 성공. 전체 마켓 수: {}", markets.size());
                    
                    // KRW 마켓만 필터링
                    List<UpbitMarketDto> krwMarkets = markets.stream()
                        .filter(market -> market.getMarket().startsWith("KRW-"))
                        .collect(Collectors.toList());
                    
                    log.info("KRW 마켓 필터링 완료. KRW 마켓 수: {}", krwMarkets.size());
                    
                    if (!krwMarkets.isEmpty()) {
                        log.info("첫 번째 KRW 마켓 샘플: {} - {}", 
                            krwMarkets.get(0).getMarket(), krwMarkets.get(0).getKorean_name());
                    }
                    
                    return krwMarkets;
                    
                } catch (Exception jsonException) {
                    log.error("JSON 파싱 실패", jsonException);
                    log.error("파싱 실패한 응답 내용: {}", responseBody);
                    return List.of();
                }
                
            } else {
                log.error("업비트 마켓 조회 실패. 상태코드: {}, 응답: {}", 
                    response.getStatusCode(), response.getBody());
                return List.of();
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("네트워크 연결 실패 (타임아웃, DNS 해석 실패 등): {}", e.getMessage());
            log.error("연결 대상 URL: {}", url);
            return List.of();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP 클라이언트 오류 (4xx): 상태코드={}, 응답={}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("HTTP 서버 오류 (5xx): 상태코드={}, 응답={}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getClass().getSimpleName(), e);
            return List.of();
        }
    }

    /**
     * 업비트 현재가 정보 조회
     * @param markets 마켓 코드 리스트
     * @return 현재가 정보 리스트
     */
    public List<UpbitTickerDto> getTickers(List<String> markets) {
        String marketParam = String.join(",", markets);
        String url = UPBIT_API_BASE_URL + TICKER_ENDPOINT + "?markets=" + marketParam;
        
        log.info("업비트 현재가 API 호출 시작");
        log.info("요청 URL: {}", url);
        log.info("요청 마켓 수: {}", markets.size());
        log.info("요청 마켓 샘플: {}", markets.stream().limit(3).collect(Collectors.toList()));
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            headers.add("User-Agent", "Java-RestTemplate");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("HTTP 요청 전송 중...");
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            log.info("업비트 현재가 API 응답 상태코드: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("현재가 API 응답 본문 길이: {} characters", responseBody != null ? responseBody.length() : 0);
                
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    log.error("현재가 API 응답 본문이 비어있음");
                    return List.of();
                }
                
                // 응답 본문 일부 로깅 (처음 500자)
                String preview = responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
                log.info("현재가 API 응답 본문 미리보기: {}", preview);
                
                try {
                    List<UpbitTickerDto> tickers = objectMapper.readValue(
                        responseBody, new TypeReference<List<UpbitTickerDto>>() {});
                    
                    log.info("현재가 JSON 파싱 성공. 조회된 현재가 데이터 수: {}", tickers.size());
                    
                    if (!tickers.isEmpty()) {
                        UpbitTickerDto sample = tickers.get(0);
                        log.info("첫 번째 현재가 데이터 샘플: {} - 현재가: {}, 등락률: {}%", 
                            sample.getMarket(), sample.getTrade_price(), 
                            sample.getChange_rate() != null ? sample.getChange_rate() * 100 : "N/A");
                    }
                    
                    return tickers;
                    
                } catch (Exception jsonException) {
                    log.error("현재가 JSON 파싱 실패", jsonException);
                    log.error("파싱 실패한 현재가 응답 내용: {}", responseBody);
                    return List.of();
                }
                
            } else {
                log.error("업비트 현재가 조회 실패. 상태코드: {}, 응답: {}", 
                    response.getStatusCode(), response.getBody());
                return List.of();
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("현재가 API 네트워크 연결 실패: {}", e.getMessage());
            return List.of();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("현재가 API HTTP 클라이언트 오류 (4xx): 상태코드={}, 응답={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("현재가 API HTTP 서버 오류 (5xx): 상태코드={}, 응답={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("현재가 API 예상치 못한 오류: {}", e.getClass().getSimpleName(), e);
            return List.of();
        }
    }

    /**
     * 모든 KRW 마켓의 현재가 정보 조회 (배치 처리)
     * @return 현재가 정보 리스트
     */
    public List<UpbitTickerDto> getAllKrwTickers() {
        log.info("=== 업비트 전체 KRW 마켓 현재가 조회 시작 (배치 처리) ===");
        
        List<UpbitMarketDto> markets = getAllMarkets();
        
        if (markets.isEmpty()) {
            log.warn("조회된 마켓이 없습니다. API 호출 실패 가능성");
            return List.of();
        }

        List<String> marketCodes = markets.stream()
            .map(UpbitMarketDto::getMarket)
            .collect(Collectors.toList());

        log.info("마켓 코드 리스트 준비 완료: {}개", marketCodes.size());
        log.info("마켓 코드 샘플: {}", marketCodes.stream().limit(5).collect(Collectors.toList()));

        // 9개씩 배치로 나누어 처리
        List<UpbitTickerDto> allTickers = new java.util.ArrayList<>();
        int batchSize = 9;
        int totalBatches = (int) Math.ceil((double) marketCodes.size() / batchSize);
        
        log.info("배치 처리 시작: 총 {}개 마켓을 {}개씩 {}번의 배치로 처리", 
            marketCodes.size(), batchSize, totalBatches);
        
        for (int i = 0; i < marketCodes.size(); i += batchSize) {
            int batchNumber = (i / batchSize) + 1;
            int endIndex = Math.min(i + batchSize, marketCodes.size());
            List<String> batch = marketCodes.subList(i, endIndex);
            
            log.info("배치 {}/{} 처리 중: {}개 마켓 ({} ~ {})", 
                batchNumber, totalBatches, batch.size(), i + 1, endIndex);
            log.debug("배치 {} 마켓 목록: {}", batchNumber, batch);
            
            try {
                List<UpbitTickerDto> batchTickers = getTickers(batch);
                allTickers.addAll(batchTickers);
                
                log.info("배치 {}/{} 완료: {}개 마켓 요청 → {}개 Ticker 응답", 
                    batchNumber, totalBatches, batch.size(), batchTickers.size());
                
                // API 호출 제한을 고려해 약간의 지연 추가 (100ms)
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("배치 {}/{} 처리 중 오류 발생: {}", batchNumber, totalBatches, e.getMessage());
                // 하나의 배치가 실패해도 계속 진행
                continue;
            }
        }
        
        log.info("=== 업비트 전체 KRW 마켓 현재가 조회 완료 ===");
        log.info("최종 결과: {}개 마켓 → {}개 Ticker 수집", marketCodes.size(), allTickers.size());
        
        return allTickers;
    }

    /**
     * 테스트용 단일 마켓 조회
     */
    public UpbitTickerDto getTickerForTest(String market) {
        log.info("테스트용 단일 마켓 조회: {}", market);
        List<UpbitTickerDto> tickers = getTickers(List.of(market));
        return tickers.isEmpty() ? null : tickers.get(0);
    }

    /**
     * 일봉 캔들 데이터 조회
     * @param market 마켓 코드 (예: KRW-BTC)
     * @param count 조회할 개수 (최대 200개)
     * @param to 마지막 캔들 시각 (YYYY-MM-DD 형식, null이면 최신부터)
     * @return 일봉 캔들 데이터 리스트
     */
    public List<UpbitCandleDto> getDayCandles(String market, int count, String to) {
        StringBuilder urlBuilder = new StringBuilder(UPBIT_API_BASE_URL + CANDLES_DAYS_ENDPOINT);
        urlBuilder.append("?market=").append(market);
        urlBuilder.append("&count=").append(Math.min(count, 200)); // 최대 200개 제한
        
        if (to != null && !to.trim().isEmpty()) {
            // YYYY-MM-DD 형식을 YYYY-MM-DDTHH:MM:SSZ 형식으로 변환
            urlBuilder.append("&to=").append(to).append("T00:00:00Z");
        }
        
        String url = urlBuilder.toString();
        log.info("업비트 일봉 캔들 API 호출: {}", url);
        
        return getCandleData(url, market, "일봉");
    }

    /**
     * 분봉 캔들 데이터 조회
     * @param unit 분 단위 (1, 3, 5, 10, 15, 30, 60, 240)
     * @param market 마켓 코드 (예: KRW-BTC)
     * @param count 조회할 개수 (최대 200개)
     * @param to 마지막 캔들 시각 (YYYY-MM-DDTHH:MM:SS 형식, null이면 최신부터)
     * @return 분봉 캔들 데이터 리스트
     */
    public List<UpbitCandleDto> getMinuteCandles(int unit, String market, int count, String to) {
        // 지원하는 분 단위 검증
        List<Integer> supportedUnits = List.of(1, 3, 5, 10, 15, 30, 60, 240);
        if (!supportedUnits.contains(unit)) {
            log.error("지원하지 않는 분 단위: {}. 지원 단위: {}", unit, supportedUnits);
            return List.of();
        }
        
        StringBuilder urlBuilder = new StringBuilder(UPBIT_API_BASE_URL + CANDLES_MINUTES_ENDPOINT + "/" + unit);
        urlBuilder.append("?market=").append(market);
        urlBuilder.append("&count=").append(Math.min(count, 200)); // 최대 200개 제한
        
        if (to != null && !to.trim().isEmpty()) {
            urlBuilder.append("&to=").append(to);
        }
        
        String url = urlBuilder.toString();
        log.info("업비트 {}분봉 캔들 API 호출: {}", unit, url);
        
        return getCandleData(url, market, unit + "분봉");
    }

    /**
     * 캔들 데이터 공통 조회 메서드
     */
    private List<UpbitCandleDto> getCandleData(String url, String market, String candleType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            headers.add("User-Agent", "Java-RestTemplate");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            log.info("업비트 {} 캔들 API 응답 상태코드: {}", candleType, response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    log.error("{} 캔들 API 응답 본문이 비어있음", candleType);
                    return List.of();
                }
                
                try {
                    List<UpbitCandleDto> candles = objectMapper.readValue(
                        responseBody, new TypeReference<List<UpbitCandleDto>>() {});
                    
                    log.info("{} 캔들 데이터 조회 성공: {}개", candleType, candles.size());
                    
                    if (!candles.isEmpty()) {
                        UpbitCandleDto firstCandle = candles.get(0);
                        log.info("첫 번째 {} 캔들: {} - 시간: {}, 시가: {}, 종가: {}", 
                            candleType, market, firstCandle.getCandleDateTimeKst(), 
                            firstCandle.getOpeningPrice(), firstCandle.getTradePrice());
                    }
                    
                    return candles;
                    
                } catch (Exception jsonException) {
                    log.error("{} 캔들 JSON 파싱 실패", candleType, jsonException);
                    return List.of();
                }
                
            } else {
                log.error("{} 캔들 조회 실패. 상태코드: {}, 응답: {}", 
                    candleType, response.getStatusCode(), response.getBody());
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("{} 캔들 API 호출 중 오류 발생", candleType, e);
            return List.of();
        }
    }

    /**
     * 한달치 일봉 데이터 조회 (약 30개)
     * @param market 마켓 코드
     * @return 최근 30일간의 일봉 데이터
     */
    public List<UpbitCandleDto> getMonthlyDayCandles(String market) {
        log.info("한달치 일봉 데이터 조회: {}", market);
        return getDayCandles(market, 30, null);
    }

    /**
     * 모든 KRW 마켓의 한달치 일봉 데이터 조회
     * @return 모든 마켓의 한달치 일봉 데이터
     */
    public List<UpbitCandleDto> getAllMarketsMonthlyCandles() {
        log.info("=== 모든 KRW 마켓 한달치 일봉 데이터 조회 시작 ===");
        
        List<UpbitMarketDto> markets = getAllMarkets();
        if (markets.isEmpty()) {
            log.warn("조회된 마켓이 없습니다.");
            return List.of();
        }

        List<UpbitCandleDto> allCandles = new java.util.ArrayList<>();
        int processedCount = 0;
        int totalMarkets = markets.size();

        for (UpbitMarketDto market : markets) {
            processedCount++;
            try {
                log.info("마켓 {}/{} 처리 중: {}", processedCount, totalMarkets, market.getMarket());
                
                List<UpbitCandleDto> marketCandles = getMonthlyDayCandles(market.getMarket());
                allCandles.addAll(marketCandles);
                
                log.info("마켓 {} 완료: {}개 캔들 수집", market.getMarket(), marketCandles.size());
                
                // API 호출 제한을 고려해 200ms 지연
                Thread.sleep(200);
                
            } catch (Exception e) {
                log.error("마켓 {} 캔들 조회 중 오류 발생: {}", market.getMarket(), e.getMessage());
                continue;
            }
        }
        
        log.info("=== 모든 KRW 마켓 한달치 일봉 데이터 조회 완료 ===");
        log.info("총 {}개 마켓에서 {}개 캔들 수집", totalMarkets, allCandles.size());
        
        return allCandles;
    }

    /**
     * 실시간 현재가 데이터 조회 (단일 종목) - Ticker API 사용
     * @param market 마켓 코드
     * @return 실시간 현재가 데이터
     */
    public UpbitTickerDto getRealtimeTicker(String market) {
        log.info("실시간 현재가 데이터 조회: {}", market);
        
        try {
            List<UpbitTickerDto> tickers = getTickers(List.of(market));
            
            if (tickers.isEmpty()) {
                log.warn("마켓 {} 실시간 현재가 데이터 없음", market);
                return null;
            }
            
            UpbitTickerDto ticker = tickers.get(0);
            log.info("실시간 현재가 조회 성공: {} - 현재가: {}, 등락률: {}%", 
                market, ticker.getTrade_price(), 
                ticker.getChange_rate() != null ? ticker.getChange_rate() * 100 : "N/A");
            
            return ticker;
            
        } catch (Exception e) {
            log.error("실시간 현재가 조회 실패: {}", market, e);
            return null;
        }
    }

    /**
     * 실시간 1분봉 데이터 조회 (가장 최신 1개) - 디버깅 강화
     * @param market 마켓 코드
     * @return 최신 1분봉 데이터
     */
    public UpbitCandleDto getLatestMinuteCandle(String market) {
        log.info("실시간 1분봉 데이터 조회 시작: {}", market);
        
        try {
            List<UpbitCandleDto> candles = getMinuteCandles(1, market, 1, null);
            
            if (candles.isEmpty()) {
                log.warn("1분봉 캔들 데이터가 비어있음: {}", market);
                return null;
            }
            
            UpbitCandleDto latestCandle = candles.get(0);
            
            log.info("1분봉 캔들 데이터 조회 성공: {} - 시간: {}, 종가: {}", 
                market, latestCandle.getCandleDateTimeKst(), latestCandle.getTradePrice());
            
            return latestCandle;
            
        } catch (Exception e) {
            log.error("1분봉 캔들 데이터 조회 실패: {}", market, e);
            return null;
        }
    }

    /**
     * 모든 KRW 마켓의 실시간 현재가 데이터 조회 - Ticker API 사용
     * @return 모든 마켓의 실시간 현재가 데이터
     */
    public List<UpbitTickerDto> getAllRealtimeTickers() {
        log.info("=== 모든 KRW 마켓 실시간 현재가 데이터 조회 시작 ===");
        
        try {
            // 모든 KRW 마켓의 현재가를 한번에 조회 (더 효율적)
            List<UpbitTickerDto> allTickers = getAllKrwTickers();
            
            log.info("=== 모든 KRW 마켓 실시간 현재가 데이터 조회 완료: {}건 ===", allTickers.size());
            
            return allTickers;
            
        } catch (Exception e) {
            log.error("모든 마켓 실시간 현재가 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 모든 KRW 마켓의 실시간 1분봉 데이터 조회
     * @return 모든 마켓의 최신 1분봉 데이터
     */
    public List<UpbitCandleDto> getAllMarketsLatestCandles() {
        log.info("=== 모든 KRW 마켓 실시간 1분봉 데이터 조회 시작 ===");
        
        List<UpbitMarketDto> markets = getAllMarkets();
        if (markets.isEmpty()) {
            log.warn("조회된 마켓이 없습니다.");
            return List.of();
        }

        List<UpbitCandleDto> allCandles = new java.util.ArrayList<>();
        int processedCount = 0;
        int totalMarkets = markets.size();

        for (UpbitMarketDto market : markets) {
            processedCount++;
            try {
                log.debug("마켓 {}/{} 실시간 데이터 조회 중: {}", processedCount, totalMarkets, market.getMarket());
                
                UpbitCandleDto latestCandle = getLatestMinuteCandle(market.getMarket());
                if (latestCandle != null) {
                    allCandles.add(latestCandle);
                }
                
                // API 호출 제한을 고려해 100ms 지연 (실시간이므로 빠르게)
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("마켓 {} 실시간 데이터 조회 중 오류 발생: {}", market.getMarket(), e.getMessage());
                continue;
            }
        }
        
        log.info("=== 모든 KRW 마켓 실시간 1분봉 데이터 조회 완료 ===");
        log.info("총 {}개 마켓에서 {}개 실시간 캔들 수집", totalMarkets, allCandles.size());
        
        return allCandles;
    }
}

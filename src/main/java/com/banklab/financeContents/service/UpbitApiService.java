package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.BitcoinTickerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Arrays;

/**
 * 업비트(Upbit) 암호화폐 거래소 공개 API를 호출하는 서비스 클래스
 * 
 * <p>이 서비스는 업비트의 공개 API를 통해 실시간 암호화폐 시세 정보를 조회합니다.
 * 인증키가 필요하지 않은 공개 API만을 사용하므로 별도의 회원가입이나 API 키 발급 없이 사용 가능합니다.</p>
 */
@Service
public class UpbitApiService {
    
    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger logger = LoggerFactory.getLogger(UpbitApiService.class);
    
    /** HTTP 요청을 처리하는 RestTemplate */
    private final RestTemplate restTemplate;
    
    /** JSON 데이터를 Java 객체로 변환하는 ObjectMapper */
    private final ObjectMapper objectMapper;
    
    /** 업비트 API 기본 URL - 비트코인 시세 조회용 */
    private static final String UPBIT_API_URL = "https://api.upbit.com/v1/ticker?markets=KRW-BTC";
    
    /**
     * 생성자 - 의존성 주입을 통해 RestTemplate을 받습니다.
     * 
     * @param restTemplate Spring에서 관리하는 RestTemplate Bean
     */
    @Autowired
    public UpbitApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        
        logger.info("UpbitApiService가 초기화되었습니다.");
        logger.debug("API 기본 URL: {}", UPBIT_API_URL);
    }
    
    /**
     * 비트코인(KRW-BTC)의 상세 시세 정보를 가져옵니다.
     */
    public BitcoinTickerDTO getBitcoinTicker() {
        try {
            logger.info("업비트 API 호출 시작: {}", UPBIT_API_URL);
            
            // HTTP 헤더 설정 - API 서버에서 요구하는 형식으로 설정
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            
            // API 호출 실행
            ResponseEntity<String> response = restTemplate.exchange(
                UPBIT_API_URL, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            logger.info("업비트 API 응답 상태: {}", response.getStatusCode());
            logger.debug("업비트 API 응답 데이터: {}", response.getBody());
            
            // 응답 성공 여부 확인
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // JSON 응답을 List<BitcoinTickerDTO>로 변환
                // 업비트 API는 단일 항목도 배열 형태로 응답함
                List<BitcoinTickerDTO> tickerList = objectMapper.readValue(
                    response.getBody(), 
                    new TypeReference<List<BitcoinTickerDTO>>() {}
                );
                
                if (!tickerList.isEmpty()) {
                    BitcoinTickerDTO ticker = tickerList.get(0);
                    logger.info("비트코인 현재가: {} KRW", ticker.getTradePrice());
                    return ticker;
                } else {
                    logger.warn("업비트 API 응답 데이터가 비어있습니다.");
                    return null;
                }
            } else {
                logger.error("업비트 API 호출 실패. 상태 코드: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("업비트 API 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 여러 암호화폐의 시세 정보를 한 번에 가져옵니다.
     *   KRW-BTC: 비트코인
     *   KRW-ETH: 이더리움
     *   KRW-XRP: 리플
     *   KRW-ADA: 에이다
     */
    public List<BitcoinTickerDTO> getMultipleTickers(String markets) {
        try {
            // 입력 검증
            if (markets == null || markets.trim().isEmpty()) {
                logger.error("마켓 코드가 비어있습니다.");
                throw new IllegalArgumentException("마켓 코드는 필수입니다.");
            }
            
            String url = "https://api.upbit.com/v1/ticker?markets=" + markets;
            logger.info("업비트 API 다중 시세 호출: {}", url);
            
            // HTTP 헤더 설정
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            
            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<BitcoinTickerDTO> tickerList = objectMapper.readValue(
                    response.getBody(), 
                    new TypeReference<List<BitcoinTickerDTO>>() {}
                );
                
                logger.info("{}개의 암호화폐 시세 정보를 가져왔습니다.", tickerList.size());
                
                // 로그로 각 종목의 현재가 출력 (디버깅용)
                tickerList.forEach(ticker -> 
                    logger.debug("{}: {} KRW", ticker.getMarket(), ticker.getTradePrice()));
                
                return tickerList;
            } else {
                logger.error("업비트 API 다중 시세 호출 실패. 상태 코드: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("업비트 API 다중 시세 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 비트코인의 현재가만 간단하게 가져옵니다.
     */
    public Double getBitcoinPrice() {
        logger.debug("비트코인 현재가 조회 요청");
        
        BitcoinTickerDTO ticker = getBitcoinTicker();
        
        if (ticker != null) {
            Double price = ticker.getTradePrice();
            logger.debug("비트코인 현재가: {} KRW", price);
            return price;
        } else {
            logger.warn("비트코인 현재가 조회 실패");
            return null;
        }
    }
    
    /**
     * 업비트 API 연결 상태를 확인합니다.
     */
    public boolean isApiAvailable() {
        try {
            logger.debug("API 연결 상태 확인 중...");
            
            BitcoinTickerDTO ticker = getBitcoinTicker();
            boolean isAvailable = ticker != null;
            
            logger.info("API 연결 상태: {}", isAvailable ? "정상" : "불가능");
            return isAvailable;
            
        } catch (Exception e) {
            logger.error("API 연결 상태 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * HTTP 요청에 사용할 공통 헤더를 생성합니다.
     */
    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // JSON 응답을 요청
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        // User-Agent 설정 (일부 API에서 필수)
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        // 추가 헤더 (필요시)
        headers.add("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8");
        
        logger.debug("HTTP 헤더 생성 완료");
        return headers;
    }
    
    /**
     * 비트코인 가격을 포맷팅하여 문자열로 반환합니다.
     */
    public String getFormattedBitcoinPrice() {
        Double price = getBitcoinPrice();
        
        if (price != null) {
            // 천 단위 콤마 추가
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
            String formattedPrice = formatter.format(price);
            
            logger.debug("포맷팅된 비트코인 가격: {} KRW", formattedPrice);
            return formattedPrice + " KRW";
        } else {
            logger.warn("비트코인 가격 정보를 가져올 수 없습니다.");
            return "가격 정보 없음";
        }
    }
    
    /**
     * 특정 암호화폐의 현재가를 조회합니다.
     */
    public Double getCryptocurrencyPrice(String marketCode) {
        // 입력 검증
        if (marketCode == null || marketCode.trim().isEmpty()) {
            logger.error("마켓 코드가 비어있습니다.");
            throw new IllegalArgumentException("마켓 코드는 필수입니다.");
        }
        
        logger.info("{} 가격 조회 요청", marketCode);
        
        List<BitcoinTickerDTO> tickers = getMultipleTickers(marketCode);
        
        if (tickers != null && !tickers.isEmpty()) {
            BitcoinTickerDTO ticker = tickers.get(0);
            Double price = ticker.getTradePrice();
            
            logger.info("{} 현재가: {} KRW", marketCode, price);
            return price;
        } else {
            logger.warn("{} 가격 조회 실패", marketCode);
            return null;
        }
    }
}

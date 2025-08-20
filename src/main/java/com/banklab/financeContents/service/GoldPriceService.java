package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.GoldApiResponseDto;
import com.banklab.financeContents.dto.GoldPriceInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 공공데이터포털 금 시세 정보 API 서비스
 * 
 * 이 서비스는 공공데이터포털(data.go.kr)의 일반상품시세정보 API를 사용하여 
 * KRX 금 시장의 실시간 금 시세 정보를 조회하는 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 전체 금 상품 목록 조회 (페이징 지원)
 * - 특정 금 상품 정보 조회
 * - 최신 금 시세 조회
 * - 최근 영업일 자동 탐색
 * 
 * API 문서: https://www.data.go.kr/data/15094805/openapi.do
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2025.01
 */
@Slf4j
@Service
public class GoldPriceService {
    
    /** 공공데이터포털 일반상품시세정보 API 엔드포인트 URL */
    private static final String API_URL = "https://apis.data.go.kr/1160100/service/GetGeneralProductInfoService/getGoldPriceInfo";
    
    /** 한 번의 API 호출로 최대 조회 가능한 데이터 개수 */
    private static final int MAX_API_ROWS = 1000;
    
    /** 최근 영업일 탐색 시 확인할 최대 일수 (주말, 공휴일 고려) */
    private static final int MAX_SEARCH_DAYS = 7;
    
    /** API 호출 시 연결 타임아웃 (밀리초) */
    private static final int CONNECT_TIMEOUT = 10000;
    
    /** API 호출 시 소켓 타임아웃 (밀리초) */
    private static final int SOCKET_TIMEOUT = 15000;
    
    /**
     * 공공데이터포털 API 인증키
     * application.properties에서 주입됨
     * 예: gold.api.key=your_api_key_here
     */
    @Value("${gold.api.key}")
    private String serviceKey;
    
    /** JSON 파싱을 위한 ObjectMapper 인스턴스 */
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 금 시세 정보 조회 (메인 API 호출 메서드)
     * 
     * 공공데이터포털 API를 호출하여 금 시세 정보를 조회합니다.
     * 모든 다른 조회 메서드들의 기반이 되는 핵심 메서드입니다.
     * 
     * @param baseDate 기준일자 (YYYYMMDD 형식)
     *                 - null 또는 빈 문자열인 경우 전일 기준으로 자동 설정
     *                 - 예: "20250122"
     * @param productCode 상품코드
     *                    - 특정 금 상품만 조회하고 싶을 때 사용
     *                    - null인 경우 전체 상품 조회
     * @param numOfRows 한 페이지당 조회할 데이터 개수 (1~1000)
     *                  - 기본값: 10
     *                  - 대용량 데이터 조회시 성능 고려 필요
     * @param pageNo 페이지 번호 (1부터 시작)
     *               - 기본값: 1
     *               - 페이징 처리로 대용량 데이터 분할 조회 가능
     * 
     * @return 금 시세 정보 리스트
     *         - 조회 성공시: 금 시세 정보가 담긴 List<GoldPriceInfoDto>
     *         - 데이터 없음: 빈 리스트 또는 null
     * 
     * @throws RuntimeException API 호출 실패, 네트워크 오류, 파싱 오류 등의 경우
     * 
     * @see GoldPriceInfoDto 반환되는 금 시세 정보 DTO
     */
    public List<GoldPriceInfoDto> getGoldPriceInfo(String baseDate, String productCode, Integer numOfRows, Integer pageNo) {
        try {
            // === 1. 파라미터 검증 및 기본값 설정 ===
            if (baseDate == null || baseDate.trim().isEmpty()) {
                // 기준일자가 없으면 어제 날짜로 설정 (거래소는 T+1 정산)
                baseDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            if (numOfRows == null || numOfRows <= 0) {
                numOfRows = 10; // 기본 조회 개수
            }
            if (pageNo == null || pageNo <= 0) {
                pageNo = 1; // 기본 페이지
            }
            
            // API 키 존재 여부 확인
            if (serviceKey == null || serviceKey.trim().isEmpty()) {
                throw new RuntimeException("API 키가 설정되지 않았습니다. application.properties의 gold.api.key를 확인해주세요.");
            }
            
            // 요청 파라미터를 최대 허용치로 제한
            if (numOfRows > MAX_API_ROWS) {
                numOfRows = MAX_API_ROWS;
                // log.warn("⚠️ 요청 데이터 개수가 최대치를 초과하여 {}개로 제한됩니다.", MAX_API_ROWS);
            }
            
            // === 2. 요청 정보 로깅 (디버깅용) ===
            // log.debug("🔑 API 키 길이: {}자 (처음 10자: {}...)", 
            //     serviceKey.length(), 
            //     serviceKey.length() > 10 ? serviceKey.substring(0, 10) : serviceKey);
            // log.info("📅 기준일자: {}", baseDate);
            // log.info("🏆 상품코드: {}", productCode != null ? productCode : "전체 상품");
            // log.info("📄 조회설정: {}개/페이지, {}페이지", numOfRows, pageNo);
            
            // === 3. API URL 구성 ===
            URI uri = buildApiUri(baseDate, productCode, numOfRows, pageNo);
            // log.info("🌐 API 호출 URL: {}", uri.toString());
            
            // === 4. HTTP 클라이언트 설정 및 요청 수행 ===
            // 타임아웃 및 프록시 설정이 포함된 HTTP 클라이언트 생성
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)     // 연결 타임아웃: 30초
                    .setSocketTimeout(SOCKET_TIMEOUT)       // 소켓 타임아웃: 30초
                    .setConnectionRequestTimeout(CONNECT_TIMEOUT)  // 연결 요청 타임아웃: 30초
                    .build();
                    
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .build()) {
                
                // HTTP GET 요청 생성 및 헤더 설정
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                httpGet.setHeader("Accept-Encoding", "gzip, deflate");
                httpGet.setHeader("Connection", "keep-alive");
                
                // log.info("📤 HTTP 요청 전송 중...");
                
                // === 5. API 호출 및 응답 처리 ===
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    
                    // log.info("📥 API 응답 상태코드: {}", statusCode);
                    // log.debug("📥 API 응답 내용 (처음 500자): {}", 
                    //     responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
                    
                    if (statusCode == 200) {
                        // HTTP 200 OK - 응답 파싱 시도
                        return parseApiResponse(responseBody);
                    } else {
                        // HTTP 오류 상태코드 처리
                        // log.error("❌ API 호출 실패. HTTP 상태코드: {}, 응답: {}", statusCode, responseBody);
                        throw new RuntimeException(String.format("API 호출 실패: HTTP %d - %s", statusCode, responseBody));
                    }
                }
            }
            
        } catch (Exception e) {
            // 모든 예외를 포착하여 로깅 후 RuntimeException으로 변환
            // log.error("❌ 금 시세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("금 시세 정보 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 최신 금 시세 조회
     * 
     * 최근 영업일의 금 시세 정보를 조회합니다.
     * 데이터가 없으면 이전 날짜로 이동하여 재시도합니다 (최대 7일).
     * 
     * @param numOfRows 조회할 상품 수 (기본: 10)
     * 
     * @return 최신 금 시세 정보 리스트
     *         - 성공시: 최신 금 시세 정보들의 List<GoldPriceInfoDto>
     *         - 실패시: null (최근 7일간 데이터 없음)
     * 
     * @see #getGoldPriceInfo(String, String, Integer, Integer) 기본 API 호출 메서드
     */
    public List<GoldPriceInfoDto> getLatestGoldPrices(int numOfRows) {
        // log.info("🏆 최신 금 시세 {}개 조회 요청", numOfRows);
        
        // === 1. 최근 영업일 데이터 탐색 ===
        LocalDate currentDate = LocalDate.now().minusDays(1); // 어제부터 시작
        
        for (int dayOffset = 0; dayOffset < MAX_SEARCH_DAYS; dayOffset++) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // log.info("📅 {} 데이터 조회 시도 중... ({}일 전)", dateStr, dayOffset + 1);
            
            try {
                List<GoldPriceInfoDto> result = getGoldPriceInfo(dateStr, null, numOfRows, 1);
                
                if (result != null && !result.isEmpty()) {
                    // log.info("✅ {} 데이터 {}개 조회 성공", dateStr, result.size());
                    
                    // 상위 금 상품 정보 로깅 (처음 3개만)
                    for (int i = 0; i < Math.min(3, result.size()); i++) {
                        GoldPriceInfoDto gold = result.get(i);
                        // log.info("  {}. {} - 종가: {}원", i + 1, gold.getProductName(), gold.getClosePrice());
                    }
                    
                    return result;
                }
                
                // log.warn("⚠️ {} 데이터 없음, 이전 날짜 시도...", dateStr);
            } catch (Exception e) {
                // log.warn("⚠️ {} 데이터 조회 중 오류: {}", dateStr, e.getMessage());
            }
            
            currentDate = currentDate.minusDays(1);
        }
        
        // log.error("❌ 최근 {}일간 금 시세 데이터를 찾을 수 없습니다.", MAX_SEARCH_DAYS);
        return null;
    }
    
    /**
     * 특정 금 상품 정보 조회
     * 
     * @param productCode 상품코드
     * @return 금 상품 정보 또는 null
     */
    public GoldPriceInfoDto getGoldPriceByProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 코드는 필수입니다.");
        }
        
        String targetCode = productCode.trim();
        // log.info("🔍 금 상품 {} 검색 시작", targetCode);
        
        // 최근 영업일 데이터 탐색
        LocalDate currentDate = LocalDate.now().minusDays(1);
        
        for (int dayOffset = 0; dayOffset < MAX_SEARCH_DAYS; dayOffset++) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // log.info("📅 {} 데이터에서 상품 {} 검색 중...", dateStr, targetCode);
            
            try {
                List<GoldPriceInfoDto> result = getGoldPriceInfo(dateStr, targetCode, 1, 1);
                if (result != null && !result.isEmpty()) {
                    GoldPriceInfoDto found = result.get(0);
                    // log.info("✅ 금 상품 {} 찾음: {}", targetCode, found.getProductName());
                    return found;
                }
            } catch (Exception e) {
                // log.warn("⚠️ {} 금 상품 조회 중 오류: {}", dateStr, e.getMessage());
            }
            
            currentDate = currentDate.minusDays(1);
        }
        
        // log.error("❌ 금 상품 {} - 최근 {}일간 데이터를 찾을 수 없습니다.", targetCode, MAX_SEARCH_DAYS);
        return null;
    }
    
    /**
     * API URI 구성 메서드
     * 
     * @param baseDate 기준일자
     * @param productCode 상품코드 (선택사항)
     * @param numOfRows 조회할 데이터 개수
     * @param pageNo 페이지 번호
     * 
     * @return 완성된 API 호출 URI
     * @throws URISyntaxException URI 구성 실패시
     */
    private URI buildApiUri(String baseDate, String productCode, int numOfRows, int pageNo) throws URISyntaxException {
        try {
            URIBuilder uriBuilder = new URIBuilder(API_URL);
            
            // === 필수 매개변수 추가 (API 문서에 따른 정확한 순서) ===
            uriBuilder.addParameter("serviceKey", serviceKey);              // API 인증키
            uriBuilder.addParameter("pageNo", String.valueOf(pageNo));       // 페이지 번호
            uriBuilder.addParameter("numOfRows", String.valueOf(numOfRows)); // 조회 개수
            uriBuilder.addParameter("resultType", "json");                  // 응답 형식: JSON
            
            // === 기준일자 추가 (선택적이지만 보통 필요) ===
            if (baseDate != null && !baseDate.trim().isEmpty()) {
                uriBuilder.addParameter("basDt", baseDate);                 // 기준일자
            }
            
            // === 선택적 매개변수 추가 ===
            if (productCode != null && !productCode.trim().isEmpty()) {
                // 특정 상품 코드가 지정된 경우 추가
                uriBuilder.addParameter("prductCd", productCode.trim()); // 상품코드
            }
            
            URI uri = uriBuilder.build();
            // log.debug("🔗 구성된 URI: {}", uri.toString());
            return uri;
            
        } catch (Exception e) {
            // log.error("❌ URI 구성 중 오류 발생: {}", e.getMessage(), e);
            throw new URISyntaxException(API_URL, "URI 구성 실패: " + e.getMessage());
        }
    }
    
    /**
     * API 응답 파싱 메서드
     * 
     * @param responseBody API 응답 본문 (JSON 또는 XML)
     * @return 파싱된 금 시세 정보 리스트
     * @throws IOException JSON 파싱 실패시
     * @throws RuntimeException API 오류 응답시
     */
    private List<GoldPriceInfoDto> parseApiResponse(String responseBody) throws IOException {
        responseBody = responseBody.trim();
        
        // === 1. XML 오류 응답 체크 ===
        if (responseBody.startsWith("<")) {
            // log.error("❌ API에서 XML 오류 응답 수신: {}", responseBody);
            
            // 일반적인 API 오류 메시지들 체크
            if (responseBody.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
                throw new RuntimeException("API 키가 등록되지 않았거나 유효하지 않습니다. 공공데이터포털에서 API 키를 확인해주세요.");
            }
            
            if (responseBody.contains("SERVICE ERROR")) {
                throw new RuntimeException("API 서비스 오류가 발생했습니다. API 키 및 요청 파라미터를 확인해주세요.");
            }
            
            if (responseBody.contains("LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR")) {
                throw new RuntimeException("일일 API 호출 한도를 초과했습니다. 내일 다시 시도해주세요.");
            }
            
            throw new RuntimeException("API에서 XML 오류 응답을 받았습니다. JSON 형식을 요청했지만 오류가 발생했습니다.");
        }
        
        // === 2. JSON 응답 파싱 ===
        // log.debug("📋 JSON 응답 파싱 시도");
        try {
            GoldApiResponseDto apiResponse = objectMapper.readValue(responseBody, GoldApiResponseDto.class);
            return extractGoldData(apiResponse);
        } catch (Exception e) {
            // log.error("❌ JSON 파싱 실패: {}", e.getMessage());
            // log.error("📄 응답 내용: {}", responseBody);
            throw new RuntimeException("JSON 응답 파싱 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * API 응답 DTO에서 금 시세 데이터 추출
     * 
     * @param apiResponse 파싱된 API 응답 DTO
     * @return 추출된 금 시세 정보 리스트
     * @throws RuntimeException API 오류 코드 발생시
     */
    private List<GoldPriceInfoDto> extractGoldData(GoldApiResponseDto apiResponse) {
        // === 1. 응답 구조 검증 ===
        if (apiResponse.getResponse() == null) {
            // log.warn("⚠️ API 응답에서 response가 null입니다.");
            return null;
        }
        
        // === 2. 헤더 정보 확인 (결과 코드) ===
        GoldApiResponseDto.ResponseBody.Header header = apiResponse.getResponse().getHeader();
        if (header != null && !"00".equals(header.getResultCode())) {
            // log.warn("⚠️ API 호출 결과 오류. 코드: {}, 메시지: {}", 
            //         header.getResultCode(), header.getResultMsg());
            throw new RuntimeException("API 오류: " + header.getResultMsg());
        }
        
        // === 3. 본문 데이터 추출 ===
        GoldApiResponseDto.ResponseBody.Body body = apiResponse.getResponse().getBody();
        if (body == null || body.getItems() == null) {
            // log.warn("⚠️ API 응답에서 데이터가 없습니다.");
            return null;
        }
        
        List<GoldPriceInfoDto> goldList = body.getItems().getItem();
        
        // === 4. 추출 결과 로깅 ===
        if (goldList != null && !goldList.isEmpty()) {
            // log.info("📊 {}개의 금 시세 데이터 추출 완료", goldList.size());
        } else {
            // log.info("📭 추출된 금 시세 데이터가 없습니다.");
        }
        
        return goldList;
    }
}

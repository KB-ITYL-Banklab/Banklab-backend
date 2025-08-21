package com.banklab.financeContents.service;
import com.banklab.financeContents.dto.StockApiResponseDto;
import com.banklab.financeContents.dto.StockSecurityInfoDto;
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
 * 공공데이터포털 주식 정보 API 서비스
 *
 * 이 서비스는 공공데이터포털(data.go.kr)의 주식시세정보 API를 사용하여
 * 실시간 주식 정보를 조회하는 기능을 제공합니다.
 *
 * 주요 기능:
 * - 전체 주식 목록 조회 (페이징 지원)
 * - 특정 종목 정보 조회
 * - 상위 N개 종목 조회
 * - 최근 영업일 자동 탐색
 *
 */
@Slf4j
@Service
public class PublicDataStockService {

    /** 공공데이터포털 주식시세정보 API 엔드포인트 URL */
    private static final String API_URL = "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo";

    /** 한 번의 API 호출로 최대 조회 가능한 데이터 개수 */
    private static final int MAX_API_ROWS = 1000;

    /** 최근 영업일 탐색 시 확인할 최대 일수 (주말, 공휴일 고려) */
    private static final int MAX_SEARCH_DAYS = 7;

    /** API 호출 시 연결 타임아웃 (밀리초) */
    private static final int CONNECT_TIMEOUT = 30000;

    /** API 호출 시 소켓 타임아웃 (밀리초) */
    private static final int SOCKET_TIMEOUT = 30000;

    /**
     * 공공데이터포털 API 인증키
     * application.properties에서 주입됨
     * 예: stock.api.key=your_api_key_here
     */
    @Value("${stock.api.key}")
    private String serviceKey;

    /** JSON 파싱을 위한 ObjectMapper 인스턴스 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 주식 가격 정보 조회 (메인 API 호출 메서드)
     *
     * 공공데이터포털 API를 호출하여 주식 시세 정보를 조회합니다.
     * 모든 다른 조회 메서드들의 기반이 되는 핵심 메서드입니다.
     *
     * @param baseDate 기준일자 (YYYYMMDD 형식)
     *                 - null 또는 빈 문자열인 경우 전일 기준으로 자동 설정
     *                 - 예: "20250122"
     * @param shortCode 단축코드 (6자리 종목코드)
     *                  - 특정 종목만 조회하고 싶을 때 사용
     *                  - null인 경우 전체 종목 조회
     *                  - 예: "005930" (삼성전자)
     * @param numOfRows 한 페이지당 조회할 데이터 개수 (1~1000)
     *                  - 기본값: 10
     *                  - 대용량 데이터 조회시 성능 고려 필요
     * @param pageNo 페이지 번호 (1부터 시작)
     *               - 기본값: 1
     *               - 페이징 처리로 대용량 데이터 분할 조회 가능
     *
     * @return 주식 정보 리스트
     *         - 조회 성공시: 주식 정보가 담긴 List<StockSecurityInfoDto>
     *         - 데이터 없음: 빈 리스트 또는 null
     *
     * @throws RuntimeException API 호출 실패, 네트워크 오류, 파싱 오류 등의 경우
     *
     * @see StockSecurityInfoDto 반환되는 주식 정보 DTO
     */
    public List<StockSecurityInfoDto> getStockPriceInfo(String baseDate, String shortCode, Integer numOfRows, Integer pageNo) {
        try {
            // === 1. 파라미터 검증 및 기본값 설정 ===
            if (baseDate == null || baseDate.trim().isEmpty()) {
                // 기준일자가 없으면 어제 날짜로 설정 (주식 시장은 T+1 정산)
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
                throw new RuntimeException("API 키가 설정되지 않았습니다. application.properties의 stock.api.key를 확인해주세요.");
            }

            // 요청 파라미터를 최대 허용치로 제한
            if (numOfRows > MAX_API_ROWS) {
                numOfRows = MAX_API_ROWS;
                // log.warn("⚠️ 요청 데이터 개수가 최대치를 초과하여 {}개로 제한됩니다.", MAX_API_ROWS);
            }

            // === 2. 요청 정보 로깅 ===
            // log.info("📅 기준일자: {}, 종목코드: {}, 조회설정: {}개/페이지, {}페이지",
            //         baseDate, shortCode != null ? shortCode : "전체", numOfRows, pageNo);

            // === 3. API URL 구성 ===
            URI uri = buildApiUri(baseDate, shortCode, numOfRows, pageNo);
            // log.info("🌐 API 요청 URL: {}", uri.toString());

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

                // === 5. API 호출 및 응답 처리 ===
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                    int statusCode = response.getStatusLine().getStatusCode();
                    // log.debug("📥 API 응답 상태코드: {}", statusCode);

                    if (statusCode == 200) {
                        // HTTP 200 OK - 응답 파싱 시도
                        // log.info("📄 API 응답 원시 데이터 (최대 1000자): {}",
                        //         responseBody.length() > 1000 ? responseBody.substring(0, 1000) + "..." : responseBody);
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
            // log.error("❌ 주식 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주식 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 종목의 주식 정보 조회
     *
     * 주어진 종목코드에 해당하는 주식 정보를 조회합니다.
     * 공공데이터 API의 특성상 직접 조회가 실패할 경우, 전체 목록에서 필터링하는 방식을 사용합니다.
     * 또한 최근 영업일 데이터를 찾기 위해 최대 7일까지 과거로 탐색합니다.
     *
     * 동작 방식:
     * 1. 지정된 종목코드로 직접 API 호출 시도
     * 2. 실패시 전체 목록 조회 후 필터링 (최대 10페이지, 1000개)
     * 3. 데이터가 없으면 이전 날짜로 이동하여 재시도 (최대 7일)
     *
     * @param shortCode 6자리 종목 단축코드 (필수)
     *                  - 예: "005930" (삼성전자), "000660" (SK하이닉스)
     *                  - null이나 빈 문자열은 허용되지 않음
     *
     * @return 주식 정보 객체
     *         - 조회 성공시: 해당 종목의 StockSecurityInfoDto 객체
     *         - 조회 실패시: null (최근 7일간 데이터 없음)
     *
     * @throws IllegalArgumentException 종목코드가 null이거나 빈 문자열인 경우
     * @throws RuntimeException API 호출 중 오류 발생시
     *
     * @see #getStockPriceInfo(String, String, Integer, Integer) 기본 API 호출 메서드
     */
    public StockSecurityInfoDto getStockInfoByCode(String shortCode) {
        // === 1. 입력 파라미터 검증 ===
        if (shortCode == null || shortCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다. 6자리 숫자를 입력해주세요. (예: 005930)");
        }

        String targetCode = shortCode.trim();
        // log.info("🔍 종목 {} 검색 시작", targetCode);

        // === 2. 최근 영업일 데이터 탐색 (최대 7일) ===
        LocalDate currentDate = LocalDate.now().minusDays(1); // 어제부터 시작

        for (int dayOffset = 0; dayOffset < MAX_SEARCH_DAYS; dayOffset++) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // log.info("📅 {} 데이터에서 종목 {} 검색 중... ({}일 전)", dateStr, targetCode, dayOffset + 1);

            // === 3. 직접 조회 시도 (1차: 종목코드 매개변수 사용) ===
            try {
                List<StockSecurityInfoDto> directResult = getStockPriceInfo(dateStr, targetCode, 1, 1);
                if (directResult != null && !directResult.isEmpty()) {
                    StockSecurityInfoDto found = directResult.get(0);

                    // 정확한 종목코드 매칭 확인 (API가 유사한 코드를 반환할 수 있음)
                    if (targetCode.equals(found.getShortCode())) {
                        // log.info("✅ 직접 조회로 종목 {} 찾음: {}", targetCode, found.getItemName());
                        return found;
                    } else {
                        // log.warn("⚠️ 직접 조회 결과 종목코드 불일치: 요청={}, 응답={}", targetCode, found.getShortCode());
                    }
                }
            } catch (Exception e) {
                // log.warn("⚠️ 직접 조회 실패, 전체 조회로 재시도: {}", e.getMessage());
            }

            // === 4. 전체 조회에서 필터링 (2차: 전체 목록에서 검색) ===
            try {
                for (int page = 1; page <= 10; page++) { // 최대 10페이지 = 1000개 검색
                    List<StockSecurityInfoDto> pageResult = getStockPriceInfo(dateStr, null, 100, page);

                    if (pageResult == null || pageResult.isEmpty()) {
                        // log.debug("📄 {}페이지에 더 이상 데이터 없음", page);
                        break; // 더 이상 데이터 없음
                    }

                    // 정확한 종목코드 매칭 찾기
                    StockSecurityInfoDto exactMatch = pageResult.stream()
                            .filter(stock -> targetCode.equals(stock.getShortCode()))
                            .findFirst()
                            .orElse(null);

                    if (exactMatch != null) {
                        // log.info("✅ 전체 조회에서 종목 {} 찾음: {} ({}페이지)",
                        //         targetCode, exactMatch.getItemName(), page);
                        return exactMatch;
                    }

                    // log.debug("🔍 {}페이지에서 종목 {} 찾지 못함 ({}/{}개 검색)",
                    //         page, targetCode, pageResult.size(), page * 100);
                }
            } catch (Exception e) {
                // log.warn("⚠️ {} 전체 조회 중 오류: {}", dateStr, e.getMessage());
            }

            // === 5. 이전 날짜로 이동 ===
            // log.warn("⚠️ {} 데이터에서 종목 {} 찾을 수 없음, 이전 날짜 시도...", dateStr, targetCode);
            currentDate = currentDate.minusDays(1);
        }

        // === 6. 최종 실패 처리 ===
        // log.error("❌ 종목 {} - 최근 {}일간 데이터를 찾을 수 없습니다.", targetCode, MAX_SEARCH_DAYS);
        return null;
    }

    /**
     * 주요 종목들의 주식 정보 조회 (상위 N개)
     *
     * 시가총액 기준 또는 거래량 기준으로 상위 종목들을 조회합니다.
     * 최근 영업일 데이터를 자동으로 탐색하여 가장 최신 데이터를 반환합니다.
     *
     * 사용 예시:
     * - 메인 화면의 주요 종목 표시
     * - 대시보드의 핵심 지표 표시
     * - 투자 분석용 기초 데이터
     *
     * @param numOfRows 조회할 종목 수 (1~100 권장)
     *                  - 너무 많은 데이터 요청시 성능 저하 가능
     *                  - 실시간 화면에서는 10~20개 권장
     *
     * @return 상위 종목 리스트
     *         - 성공시: 시가총액 상위 종목들의 List<StockSecurityInfoDto>
     *         - 실패시: null (최근 7일간 데이터 없음)
     *
     * @see #getStockPriceInfo(String, String, Integer, Integer) 기본 API 호출 메서드
     */
    public List<StockSecurityInfoDto> getTopStocks(int numOfRows) {
        // log.info("🏆 상위 {}개 종목 조회 요청", numOfRows);

        // === 1. 최근 영업일 데이터 탐색 ===
        LocalDate currentDate = LocalDate.now().minusDays(1); // 어제부터 시작

        for (int dayOffset = 0; dayOffset < MAX_SEARCH_DAYS; dayOffset++) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // log.info("📅 {} 데이터 조회 시도 중... ({}일 전)", dateStr, dayOffset + 1);

            try {
                List<StockSecurityInfoDto> result = getStockPriceInfo(dateStr, null, numOfRows, 1);

                if (result != null && !result.isEmpty()) {
                    // log.info("✅ {} 데이터 {}개 조회 성공", dateStr, result.size());

                    // 상위 종목 정보 로깅 (처음 3개만)
                    for (int i = 0; i < Math.min(3, result.size()); i++) {
                        StockSecurityInfoDto stock = result.get(i);
                        // log.info("  {}위: {} ({})", i + 1, stock.getItemName(), stock.getShortCode());
                    }

                    return result;
                }

                // log.warn("⚠️ {} 데이터 없음, 이전 날짜 시도...", dateStr);
            } catch (Exception e) {
                // log.warn("⚠️ {} 데이터 조회 중 오류: {}", dateStr, e.getMessage());
            }

            currentDate = currentDate.minusDays(1);
        }

        // log.error("❌ 최근 {}일간 상위 종목 데이터를 찾을 수 없습니다.", MAX_SEARCH_DAYS);
        return null;
    }

    /**
     * API URI 구성 메서드
     *
     * 공공데이터포털 API 호출을 위한 완전한 URI를 구성합니다.
     * URIBuilder를 사용하여 안전한 URL 인코딩을 보장합니다.
     *
     * @param baseDate 기준일자 (YYYYMMDD)
     * @param shortCode 종목코드 (선택사항)
     * @param numOfRows 조회할 데이터 개수
     * @param pageNo 페이지 번호
     *
     * @return 완성된 API 호출 URI
     * @throws URISyntaxException URI 구성 실패시
     */
    private URI buildApiUri(String baseDate, String shortCode, int numOfRows, int pageNo) throws URISyntaxException {
        try {
            URIBuilder uriBuilder = new URIBuilder(API_URL);

            // === 필수 매개변수 추가 ===
            uriBuilder.addParameter("serviceKey", serviceKey);      // API 인증키
            uriBuilder.addParameter("resultType", "json");          // 응답 형식: JSON
            uriBuilder.addParameter("basDt", baseDate);             // 기준일자
            uriBuilder.addParameter("numOfRows", String.valueOf(numOfRows)); // 조회 개수
            uriBuilder.addParameter("pageNo", String.valueOf(pageNo));       // 페이지 번호

            // === 시장 구분 제거 - 모든 시장(KOSPI, KOSDAQ) 조회 ===
            // 특정 종목 조회 시에는 시장 구분을 하지 않음
            // uriBuilder.addParameter("mrktCtg", "KOSPI");  // KOSPI 시장만
            // uriBuilder.addParameter("mrktCtg", "KOSDAQ"); // KOSDAQ 시장만

            // === 선택적 매개변수 추가 ===
            if (shortCode != null && !shortCode.trim().isEmpty()) {
                // 특정 종목 코드가 지정된 경우 추가
                uriBuilder.addParameter("srtnCd", shortCode.trim()); // 단축코드
            }

            URI uri = uriBuilder.build();
            return uri;

        } catch (Exception e) {
            // log.error("❌ URI 구성 중 오류 발생: {}", e.getMessage(), e);
            throw new URISyntaxException(API_URL, "URI 구성 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답 파싱 메서드
     *
     * 공공데이터포털에서 받은 JSON 응답을 파싱하여 주식 정보 객체로 변환합니다.
     * XML 오류 응답도 감지하여 적절한 예외 메시지를 생성합니다.
     *
     * 처리하는 오류 유형:
     * - SERVICE_KEY_IS_NOT_REGISTERED_ERROR: API 키 미등록
     * - LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR: 일일 호출 한도 초과
     * - SERVICE ERROR: 기타 서비스 오류
     *
     * @param responseBody API 응답 본문 (JSON 또는 XML)
     * @return 파싱된 주식 정보 리스트
     * @throws IOException JSON 파싱 실패시
     * @throws RuntimeException API 오류 응답시
     */
    private List<StockSecurityInfoDto> parseApiResponse(String responseBody) throws IOException {
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
            StockApiResponseDto apiResponse = objectMapper.readValue(responseBody, StockApiResponseDto.class);
            return extractStockData(apiResponse);
        } catch (Exception e) {
            // log.error("❌ JSON 파싱 실패: {}", e.getMessage());
            // log.error("📄 응답 내용: {}", responseBody);
            throw new RuntimeException("JSON 응답 파싱 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * API 응답 DTO에서 주식 데이터 추출
     *
     * 공공데이터포털의 표준 응답 구조에서 실제 주식 데이터를 추출합니다.
     * 응답 구조: response.header (결과 코드) + response.body.items.item (실제 데이터)
     *
     * @param apiResponse 파싱된 API 응답 DTO
     * @return 추출된 주식 정보 리스트
     * @throws RuntimeException API 오류 코드 발생시
     */
    private List<StockSecurityInfoDto> extractStockData(StockApiResponseDto apiResponse) {
        // === 1. 응답 구조 검증 ===
        if (apiResponse.getResponse() == null) {
            // log.warn("⚠️ API 응답에서 response가 null입니다.");
            return null;
        }

        // === 2. 헤더 정보 확인 (결과 코드) ===
        StockApiResponseDto.ResponseBody.Header header = apiResponse.getResponse().getHeader();
        if (header != null && !"00".equals(header.getResultCode())) {
            // log.warn("⚠️ API 호출 결과 오류. 코드: {}, 메시지: {}",
            //         header.getResultCode(), header.getResultMsg());
            throw new RuntimeException("API 오류: " + header.getResultMsg());
        }

        // === 3. 본문 데이터 추출 ===
        StockApiResponseDto.ResponseBody.Body body = apiResponse.getResponse().getBody();
        if (body == null || body.getItems() == null) {
            // log.warn("⚠️ API 응답에서 데이터가 없습니다.");
            return null;
        }

        List<StockSecurityInfoDto> stockList = body.getItems().getItem();

        // === 4. 추출 결과 로깅 ===
        if (stockList != null && !stockList.isEmpty()) {
            // log.info("📊 {}개의 주식 데이터 추출 완료", stockList.size());
        } else {
            // log.info("📭 추출된 주식 데이터가 없습니다.");
        }

        return stockList;
    }
}
package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.NewsItemDto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 네이버 뉴스 검색 API 서비스 클래스
 * <p>
 * OPENAPI 연결 흐름:
 * 1. application.properties에서 네이버 API 키 로드
 * 2. UriComponentsBuilder로 요청 URL 생성
 * 3. HTTP 헤더에 인증 정보 설정
 * 4. RestTemplate으로 네이버 API 호출
 * 5. JSON 응답 파싱 후 DTO 리스트로 변환
 */
@Service
public class NaverNewsService {

    // application.properties에서 네이버 Client ID 주입
    @Value("${naver.client-id}")
    private String clientId;

    // application.properties에서 네이버 Client Secret 주입
    @Value("${naver.client-secret}")
    private String clientSecret;

    // HTTP 요청을 위한 RestTemplate 의존성 주입
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 키워드로 네이버 뉴스를 검색하는 메서드
     *
     * @param keyword 검색할 키워드
     * @return 뉴스 목록 (NewsItemDto 리스트)
     */
    public List<NewsItemDto> searchNews(String keyword) {
        List<NewsItemDto> newsList = new ArrayList<>();

        try {
            // 1. 네이버 뉴스 검색 API URL 생성
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")  // 네이버 OPENAPI 베이스 URL
                    .path("/v1/search/news.json")                // 뉴스 검색 API 경로
                    .queryParam("query", keyword)               // 검색 키워드
                    .queryParam("display", 10)                  // 검색 결과 출력 건수 (1~100)
                    .queryParam("start", 1)                     // 검색 시작 위치 (1~1000)
                    .queryParam("sort", "date")                 // 정렬 옵션 (date: 날짜순)
                    .encode()                                   // URL 인코딩
                    .build()
                    .toUri();

            // 2. HTTP 헤더에 네이버 API 인증 정보 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);        // 클라이언트 ID
            headers.set("X-Naver-Client-Secret", clientSecret); // 클라이언트 시크릿

            // 3. HTTP 요청 엔티티 생성
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 4. 네이버 API 호출 (GET 방식)
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            // 5. JSON 응답 파싱
            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");   // items 배열 추출
            Gson gson = new Gson();

            // 6. JSON 배열의 각 항목을 NewsItemDto로 변환
            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                NewsItemDto dto = gson.fromJson(item, NewsItemDto.class);  // JSON -> DTO 변환

                // HTML 엔티티 디코딩 처리
                dto.setTitle(HtmlUtils.htmlUnescape(dto.getTitle()));
                dto.setDescription(HtmlUtils.htmlUnescape(dto.getDescription()));

                newsList.add(dto);
            }

        } catch (Exception e) {
            // API 호출 실패 시 예외 처리
            e.printStackTrace();
        }

        return newsList;
    }
}

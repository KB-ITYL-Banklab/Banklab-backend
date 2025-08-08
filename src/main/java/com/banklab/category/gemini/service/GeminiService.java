package com.banklab.category.gemini.service;

import com.banklab.category.gemini.dto.GeminiRequest;
import com.banklab.category.gemini.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public GeminiService(RestTemplate restTemplate,
                         @Value("${gemini.api.url}") String apiUrl,
                         @Value("${gemini.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String generateText(String prompt) {
        // 1. 요청 본문 구성
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));

        // 2. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        // 3. POST 요청
        ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                apiUrl + "?key=" + apiKey,
                HttpMethod.POST,
                requestEntity,
                GeminiResponse.class
        );

        GeminiResponse geminiResponse = response.getBody();
        if (geminiResponse != null && !geminiResponse.candidates.isEmpty()) {
            return geminiResponse.candidates.get(0).content.parts.get(0).text;
        }

        return "응답 없음";
    }

    public List<String> classifyCategories(Set<String> descriptions) {
        String joinedDescriptions = String.join(", ", descriptions);

        String prompt =
                """
                다음은 쉼표로 구분된 상호명 리스트이다.
                웹 검색을 통해 해당 상호가 어떤 업종인지 파악한 후 정확하게 분류하라.
                '기타' 카테고리로 분류되는 개수는 전체 상호명의 30%를 넘으면 안 된다.
                
                결과는 쉼표로 구분된 카테고리 이름 리스트로만 반환하며, 반드시 입력된 상호명 수와 동일한 개수의 카테고리만 쉼표로 구분해서 반환.
                그 외의 설명이나 문장은 절대 포함하지 마.카테고리는 반드시 다음 8개의 항목 중에서 하나만 선택해야 하며, 중복 선택은 불가:
                
                카페/간식
                주거/통신
                식비
                교통
                쇼핑
                문화/여가
                이체
                기타
                
                카테고리 분류 기준:
                카페/간식: 커피전문점, 제과점, 프랜차이즈 음료점, 간식류 등 (커피 또는 카페 단어가 들어간 경우)
                주거/통신: 월세, 관리비, 통신 요금, 통신사 관련 상호
                식비: 음식점, 배달앱, 배달 전문점, 포장 음식, 외식, 편의점
                교통: 버스, 지하철, 택시, 대중교통 카드, 주유, 고속도로 통행료
                쇼핑: 의류, 전자기기, 잡화, 화장품, 생활용품, 온라인 쇼핑몰
                문화/여가: 영화관, 서점, 전시회, 공연장, 게임, 콘서트, OTT 서비스
                이체: 사람 이름, 송금, 계좌이체, 금융기관 이체 관련
                기타: 검색했음에도 불구하고 위의 분류 어디에도 명확히 속하지 않는 경우
                
                예시:
                입력: 씨유, 이디야, 배달의민족, 홍길동, 교보문고, 쿠팡, SKT
                출력: 식비, 카페/간식, 식비, 이체, 문화/여가, 쇼핑, 주거/통신
                상호명:""" + joinedDescriptions;

        String geminiResponseText = generateText(prompt);

        if (geminiResponseText != null && !geminiResponseText.isEmpty()) {
            return Arrays.stream(geminiResponseText.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
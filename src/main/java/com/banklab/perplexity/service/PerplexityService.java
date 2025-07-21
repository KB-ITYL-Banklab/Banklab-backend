package com.banklab.perplexity.service;

import com.banklab.perplexity.dto.PerplexityRequestDto;
import com.banklab.perplexity.dto.PerplexityResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerplexityService {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public List<String> getCompletions(List<String> descriptions) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        String joinedDescriptions = String.join(", ", descriptions);


        String prompt =
                """
                다음은 쉼표로 구분된 상호명 리스트입니다.
                각 상호명의 업종이나 성격을 파악하여 아래 제시된 카테고리 중 하나에 정확하게 분류.
                모호하거나 정보가 부족한 경우, 웹 검색을 통해 해당 상호가 어떤 업종인지 파악한 뒤 가장 적합한 카테고리를 선택.
                
                결과는 쉼표로 구분된 카테고리 이름 리스트로만 반환하며, 그 외의 설명은 절대 포함하지 마
                카테고리는 반드시 다음 8개의 항목 중에서 하나만 선택해야 하며, 중복 선택은 불가:
                
                카페/간식
                주거/통신
                식비
                교통
                쇼핑
                문화/여가
                이체
                기타
                
                카테고리 분류 기준:
                카페/간식: 편의점, 커피전문점, 제과점, 프랜차이즈 음료점, 간식류 등 (커피 또는 카페 단어가 들어간 경우)
                주거/통신: 월세, 관리비, 통신 요금, 통신사 관련 상호
                식비: 음식점, 배달앱, 배달 전문점, 포장 음식, 외식
                교통: 버스, 지하철, 택시, 대중교통 카드, 주유, 고속도로 통행료
                쇼핑: 의류, 전자기기, 잡화, 화장품, 생활용품, 온라인 쇼핑몰
                문화/여가: 영화관, 서점, 전시회, 공연장, 게임, 콘서트, OTT 서비스
                이체: 사람 이름, 송금, 계좌이체, 금융기관 이체 관련
                기타: 검색했음에도 불구하고 위의 분류 어디에도 명확히 속하지 않는 경우
                
                예시:
                입력: 씨유, 이디야, 배달의민족, 홍길동, 교보문고, 쿠팡, SKT
                출력: 카페/간식, 카페/간식, 식비, 이체, 문화/여가, 쇼핑, 주거/통신
                상호명: [""" + joinedDescriptions + "]";


        PerplexityRequestDto.Message message = new PerplexityRequestDto.Message("user", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "sonar");
        requestBody.put("messages", Collections.singletonList(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PerplexityResponseDto> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                PerplexityResponseDto.class
        );

        String content = response.getBody().getChoices()[0].getMessage().getContent();
        return Arrays.stream(content.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

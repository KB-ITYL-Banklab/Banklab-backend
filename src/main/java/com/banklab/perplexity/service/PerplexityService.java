package com.banklab.perplexity.service;

import com.banklab.perplexity.dto.PerplexityRequestDto;
import com.banklab.perplexity.dto.PerplexityResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PerplexityService {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public PerplexityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PerplexityResponseDto getCompletion(PerplexityRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "sonar");
        requestBody.put("messages", requestDto.getMessages());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PerplexityResponseDto> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                PerplexityResponseDto.class
        );

        return response.getBody();
    }
}

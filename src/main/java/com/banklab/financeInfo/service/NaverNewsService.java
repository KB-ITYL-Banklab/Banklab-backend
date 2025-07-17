package com.banklab.financeInfo.service;

import com.banklab.financeInfo.dto.NewsItemDto;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class NaverNewsService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    public List<NewsItemDto> searchNews(String keyword) {
        List<NewsItemDto> newsList = new ArrayList<>();

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")
                    .path("/v1/search/news.json")
                    .queryParam("query", keyword)
                    .queryParam("display", 10)
                    .queryParam("start", 1)
                    .queryParam("sort", "date")
                    .encode()
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            // JSON 파싱
            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            Gson gson = new Gson();

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                NewsItemDto dto = gson.fromJson(item, NewsItemDto.class);
                newsList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }
}

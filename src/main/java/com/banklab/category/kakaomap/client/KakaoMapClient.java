package com.banklab.category.kakaomap.client;

import com.banklab.category.kakaomap.dto.KakaoMapSearchResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Log4j2
@PropertySource({"classpath:/application.properties"})
public class KakaoMapClient {
    @Value("${kakao.client-id}")
    private String CLIENT_ID;

    @Value("${kakao.client-secret}")
    private String CLIENT_SECRET;

    @Value("${kakao.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${kakao.token-uri}")
    private String TOKEN_URI;

    @Value("${kakao.keyword-search-uri}")
    private String KEYWORD_SEARCH_URI;
    private final RestTemplate restTemplate = createCustomRestTemplate();

    public KakaoMapSearchResponseDto getCategoryByDesc(String desc) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + CLIENT_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);


        String url = KEYWORD_SEARCH_URI + "?page=1&size=1&sort=accuracy&query=" + desc;
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoMapSearchResponseDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, KakaoMapSearchResponseDto.class);
        
        return response.getBody();
    }

    private RestTemplate createCustomRestTemplate() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(converter);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(messageConverters);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        return restTemplate;
    }
}
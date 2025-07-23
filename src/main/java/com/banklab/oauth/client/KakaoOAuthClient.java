package com.banklab.oauth.client;

import com.banklab.oauth.dto.KakaoTokenResponseDTO;
import com.banklab.oauth.dto.KakaoUserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@PropertySource({"classpath:/application.properties"})
public class KakaoOAuthClient {
    @Value("${kakao.client-id}")
    private String CLIENT_ID;

    @Value("${kakao.client-secret}")
    private String CLIENT_SECRET;

    @Value("${kakao.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${kakao.token-uri}")
    private String TOKEN_URI;

    @Value("${kakao.user-info-uri}")
    private String USER_INFO_URI;
    private final RestTemplate restTemplate = createCustomRestTemplate();

    public KakaoTokenResponseDTO getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", CLIENT_ID);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("code", code);
        body.add("client_secret", CLIENT_SECRET);  // 보안 강화를 위해 추가 확인 코드

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<KakaoTokenResponseDTO> response = restTemplate.postForEntity(
                TOKEN_URI, request, KakaoTokenResponseDTO.class);
        log.info(Objects.requireNonNull(response.getBody()).toString());
        return response.getBody();
    }

    public KakaoUserInfoDTO getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoUserInfoDTO> response = restTemplate.exchange(
                USER_INFO_URI, HttpMethod.GET, entity, KakaoUserInfoDTO.class);
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

package com.banklab.codef.service;

import com.banklab.codef.util.CommonConstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;


@Log4j2
public class RequestToken {

    private static final String TOKEN_URL = CommonConstant.TOKEN_DOMAIN + CommonConstant.GET_TOKEN;

    /**
     * access token 발급 요청
     *
     * @param clientId      : client id
     * @param clientSecret  : client secret
     * @return 액세스 토큰
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getAccessToken(String clientId, String clientSecret) throws IOException, InterruptedException {

        try{// 1. Base64 인코딩
            String auth = clientId + ":" + clientSecret;
            String authStringEnc = Base64.getEncoder().encodeToString(auth.getBytes());

            // 2. 토큰 요청
            String POST_PARAMS = "grant_type=client_credentials&scope=read";    // Oauth2.0 사용자 자격증명 방식(client_credentials) 토큰 요청 설정

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Authorization", "Basic " + authStringEnc)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(POST_PARAMS))
                    .build();

            // 3. 요청 전송
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("토큰 요청 응답 코드: {}", response.statusCode());
            log.info("응답 내용 : {}", response.body());


            // 4. JSON 파싱 (Jackson)
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());

                return json.get("access_token").asText(); // 토큰 반환
            }

            else {
                log.info("토큰 요청 실패: {}", response.body());
            }
        } catch (IOException | InterruptedException e) {
            log.info("토큰 요청 중 예외 발생", e);
        }

        return null;
    }
}
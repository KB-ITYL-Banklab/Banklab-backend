package com.banklab.codef.util;

import com.banklab.codef.service.RequestToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Map;
import java.time.LocalDateTime;  // 추가
import java.util.UUID;  // 추가


/**
 * API 요청 템플릿 클래스
 */
@Log4j2
public class ApiRequest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * API Reqeust
     *
     * @param urlPath : 요청할 url 경로
     * @param bodyMap : API 요청 입력부(Input)
     * @return API 요청 출력부 JSON / type : String
     * @throws IOException
     * @throws InterruptedException
     */
    public static String request(String urlPath, Map<String, Object> bodyMap) throws IOException, InterruptedException {

        // 리소스서버 접근을 위한 액세스토큰 설정 (기존에 발급 받은 토큰이 있다면 유효기간 만료까지 재사용)
        String accessToken = CommonConstant.ACCESS_TOKEN;

        // 토큰이 없으면 액세스 토큰 새로 발급
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = RequestToken.getAccessToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
            CommonConstant.ACCESS_TOKEN = accessToken;
        }

        // Json 변환
        String bodyJson = mapper.writeValueAsString(bodyMap);

        // 요청 전송
        JsonNode json = HttpRequest.post(urlPath, accessToken, bodyJson);
        String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        // 유효성 검사
        if (json.has("error") && "invalid_token".equals(json.get("error").asText())) {
            accessToken = RequestToken.getAccessToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
            CommonConstant.ACCESS_TOKEN = accessToken;

            json = (JsonNode) HttpRequest.post(urlPath, accessToken, bodyJson);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

            log.info("재시도 완료");

        }
        else if (json.has("error") && "access_denied".equals(json.get("error").asText())) {
            log.info("access_denied은 API 접근 권한이 없는 경우입니다.");
            log.info("코드에프 대시보드의 API 설정을 통해 해당 업무 접근 권한을 설정해야 합니다.");
        }

        return result;
    }
}

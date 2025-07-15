package com.banklab.codef.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 실제 HTTP 호출을 위한 재사용 클래스
 */
public class HttpRequest {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Object post(String urlPath, String token, String bodyString) {
        try {
            // HTTP 요청을 위한 URL 객체 생성
            URL url = new URL(urlPath);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // 요청 기본 설정
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            if (token != null && !token.isEmpty()) {
                con.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            // 요청 본문 전송
            try (OutputStream os = con.getOutputStream()) {
                os.write(bodyString.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 응답 코드 확인
            int responseCode = con.getResponseCode();
            InputStream is = responseCode == HttpURLConnection.HTTP_OK ?
                    con.getInputStream() : con.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            String decoded = URLDecoder.decode(response.toString(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(decoded);

            return jsonNode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

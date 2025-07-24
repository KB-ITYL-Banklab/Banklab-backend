package com.banklab.financeContents.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

/**
 * RestTemplate 설정 유틸리티 클래스
 */
@Component
public class RestTemplateUtil {
    
    /**
     * 커스텀 RestTemplate 생성
     * - 연결 타임아웃: 5초
     * - 읽기 타임아웃: 10초
     * @return RestTemplate 설정된 RestTemplate 객체
     */
    public static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 연결 타임아웃 설정 (5초)
        factory.setConnectTimeout(5000);
        
        // 읽기 타임아웃 설정 (10초)
        factory.setReadTimeout(10000);
        
        return new RestTemplate(factory);
    }
    
    /**
     * 커스텀 타임아웃을 가진 RestTemplate 생성
     * @param connectTimeout 연결 타임아웃 (밀리초)
     * @param readTimeout 읽기 타임아웃 (밀리초)
     * @return RestTemplate 설정된 RestTemplate 객체
     */
    public static RestTemplate createRestTemplate(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        
        return new RestTemplate(factory);
    }
}

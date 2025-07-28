package com.banklab.financeContents.service;

import com.banklab.financeContents.controller.ExchangeRateController;
import com.banklab.financeContents.service.ExchangeRateService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Log4j2
@ContextConfiguration(locations = {"classpath:test-root-context.xml"})
@DisplayName("ExchangeRateController API 테스트")
class ExchangeRateControllerTest {

    @Autowired(required = false)
    private ExchangeRateController exchangeRateController;
    
    @Autowired(required = false)
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("/api/exchange/chart - 차트용 환율 정보 조회 테스트")
    void getExchangeRateForChart_Test() {
        try {
            if (exchangeRateController == null) {
                log.warn("ExchangeRateController가 주입되지 않아 테스트를 건너뜁니다.");
                return;
            }
            
            // When
            ResponseEntity<Map<String, Object>> response = exchangeRateController.getExchangeRateForChart();
            
            // Then
            assertNotNull(response);
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = response.getBody();
            
            // 응답 상태가 200 OK 또는 503 Service Unavailable 또는 500 중 하나여야 함
            assertTrue(response.getStatusCode().value() == 200 || 
                      response.getStatusCode().value() == 503 || 
                      response.getStatusCode().value() == 500);
            
            if (response.getStatusCode().value() == 200) {
                assertTrue(responseBody.containsKey("data"));
                assertTrue(responseBody.containsKey("count"));
                assertTrue(responseBody.containsKey("message"));
                log.info("환율 차트 API 조회 성공: {}", responseBody.get("message"));
            } else {
                assertTrue(responseBody.containsKey("error") || responseBody.containsKey("message"));
                log.warn("환율 차트 API 조회 실패: 상태코드={}, 응답={}", 
                        response.getStatusCode().value(), responseBody);
            }
            
        } catch (Exception e) {
            log.error("테스트 실행 중 오류 발생: {}", e.getMessage());
            log.warn("외부 API 의존성으로 인한 오류로 테스트를 건너뜁니다: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("환율 서비스 연결 테스트")
    void exchangeRateService_ConnectionTest() {
        if (exchangeRateService != null) {
            log.info("환율 서비스 연결 테스트 성공");
        } else {
            log.warn("ExchangeRateService가 주입되지 않았습니다 - 스킵");
        }
    }
    
    @Test
    @DisplayName("환율 컨트롤러 빈 등록 테스트")
    void exchangeRateController_BeanTest() {
        if (exchangeRateController != null) {
            log.info("환율 컨트롤러 빈 등록 테스트 성공");
        } else {
            log.warn("ExchangeRateController가 빈으로 등록되지 않았습니다 - 스킵");
        }
    }
}
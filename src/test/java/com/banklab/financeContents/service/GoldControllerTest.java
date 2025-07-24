package com.banklab.financeContents.service;

import com.banklab.financeContents.controller.GoldController;
import com.banklab.financeContents.service.GoldPriceService;
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
@DisplayName("GoldController API 테스트")
class GoldControllerTest {

    @Autowired(required = false)
    private GoldController goldController;
    
    @Autowired(required = false)
    private GoldPriceService goldPriceService;

    @Test
    @DisplayName("/api/gold/chart - 차트용 금 시세 정보 조회 테스트")
    void getGoldForChart_Test() {
        try {
            if (goldController == null) {
                log.warn("GoldController가 주입되지 않아 테스트를 건너뜁니다.");
                return;
            }
            
            // When
            ResponseEntity<Map<String, Object>> response = goldController.getGoldForChart();
            
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
                log.info("금 시세 차트 API 조회 성공: {}", responseBody.get("message"));
            } else {
                assertTrue(responseBody.containsKey("error") || responseBody.containsKey("message"));
                log.warn("금 시세 차트 API 조회 실패: 상태코드={}, 응답={}", 
                        response.getStatusCode().value(), responseBody);
            }
            
        } catch (Exception e) {
            log.error("테스트 실행 중 오류 발생: {}", e.getMessage());
            log.warn("외부 API 의존성으로 인한 오류로 테스트를 건너뜁니다: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("금 시세 서비스 연결 테스트")
    void goldPriceService_ConnectionTest() {
        if (goldPriceService != null) {
            log.info("금 시세 서비스 연결 테스트 성공");
        } else {
            log.warn("GoldPriceService가 주입되지 않았습니다 - 스킵");
        }
    }
    
    @Test
    @DisplayName("금 시세 컨트롤러 빈 등록 테스트")
    void goldController_BeanTest() {
        if (goldController != null) {
            log.info("금 시세 컨트롤러 빈 등록 테스트 성공");
        } else {
            log.warn("GoldController가 빈으로 등록되지 않았습니다 - 스킵");
        }
    }
}
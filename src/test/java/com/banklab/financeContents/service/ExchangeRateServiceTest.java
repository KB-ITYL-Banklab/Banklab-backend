package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.ExchangeRateDto;
import com.banklab.financeContents.dto.ExchangeRateResponse;
import com.banklab.financeContents.util.ExchangeRateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 한국수출입은행 환율 API 서비스 테스트 클래스
 * 
 * 테스트 항목:
 * - 실시간 환율 조회 기능
 * - 특정 날짜 환율 조회 기능
 * - 특정 통화 환율 조회 기능
 * - API 연결 및 URL 생성 테스트
 * - 유틸리티 함수 테스트
 */
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/test/resources/test-root-context.xml", "file:src/test/resources/test-servlet-context.xml"})
@DisplayName("한국수출입은행 환율 API 서비스 테스트")
public class ExchangeRateServiceTest {
    
    @Autowired
    private ExchangeRateService exchangeRateService;
    
    /**
     * 테스트 실행 전 준비 작업
     * ExchangeRateService 인스턴스를 생성합니다.
     */
    @BeforeEach
    void setUp() {
    }
    
    /**
     * 오늘 환율 정보 조회 테스트
     * 현재 날짜의 환율 정보를 실제 API에서 가져와 검증합니다.
     */
    @Test
    @DisplayName("오늘 환율 정보 조회 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGetTodayExchangeRates() {
        // Given
        System.out.println("=== 오늘 환율 정보 조회 테스트 시작 ===");
        
        // When
        ExchangeRateResponse response = exchangeRateService.getTodayExchangeRates();
        
        // Then
        assertNotNull(response, "응답이 null이면 안됩니다");
        System.out.println("API 응답 성공 여부: " + response.isSuccess());
        System.out.println("응답 메시지: " + response.getMessage());
        System.out.println("조회 날짜: " + response.getSearchDate());
        System.out.println("데이터 개수: " + response.getCount());
        
        if (response.isSuccess()) {
            assertTrue(response.getCount() > 0, "환율 데이터가 있어야 합니다");
            assertNotNull(response.getData(), "데이터 리스트가 null이면 안됩니다");
            
            // 첫 번째 환율 정보 출력
            if (!response.getData().isEmpty()) {
                ExchangeRateDto firstRate = response.getData().get(0);
                System.out.println("첫 번째 환율 정보:");
                System.out.println("- 통화코드: " + firstRate.getCur_unit());
                System.out.println("- 통화명: " + firstRate.getCur_nm());
                System.out.println("- 매매기준율: " + firstRate.getDeal_bas_r());
                System.out.println("- TTB: " + firstRate.getTtb());
                System.out.println("- TTS: " + firstRate.getTts());
            }
        }
    }
    
    /**
     * 특정 날짜 환율 정보 조회 테스트
     * 2025년 7월 21일 환율 정보를 조회하여 검증합니다.
     */
    @Test
    @DisplayName("특정 날짜 환율 정보 조회 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGetExchangeRatesByDate() {
        // Given
        String testDate = "20250721"; // 2025년 7월 21일
        System.out.println("=== 특정 날짜 환율 정보 조회 테스트 시작 ===");
        System.out.println("조회 날짜: " + testDate);
        
        // When
        ExchangeRateResponse response = exchangeRateService.getExchangeRates(testDate);
        
        // Then
        assertNotNull(response, "응답이 null이면 안됩니다");
        System.out.println("API 응답 성공 여부: " + response.isSuccess());
        System.out.println("응답 메시지: " + response.getMessage());
        System.out.println("데이터 개수: " + response.getCount());
        
        if (response.isSuccess() && response.getCount() > 0) {
            // USD 환율 찾기 및 출력
            ExchangeRateDto usdRate = ExchangeRateUtil.findByCurrencyCode(response.getData(), "USD");
            if (usdRate != null) {
                System.out.println("USD 환율 정보:");
                System.out.println("- 통화명: " + usdRate.getCur_nm());
                System.out.println("- 매매기준율: " + usdRate.getDeal_bas_r());
                System.out.println("- TTB: " + usdRate.getTtb());
                System.out.println("- TTS: " + usdRate.getTts());
            }
        }
    }
    
    /**
     * 특정 통화 환율 정보 조회 테스트
     * USD 통화의 특정 날짜 환율 정보를 조회합니다.
     */
    @Test
    @DisplayName("특정 통화 환율 정보 조회 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGetExchangeRateByCurrency() {
        // Given
        String testDate = "20250721";
        String currency = "USD";
        System.out.println("=== 특정 통화 환율 정보 조회 테스트 시작 ===");
        System.out.println("조회 날짜: " + testDate);
        System.out.println("조회 통화: " + currency);
        
        // When
        ExchangeRateResponse response = exchangeRateService.getExchangeRateByCurrency(testDate, currency);
        
        // Then
        assertNotNull(response, "응답이 null이면 안됩니다");
        System.out.println("API 응답 성공 여부: " + response.isSuccess());
        System.out.println("응답 메시지: " + response.getMessage());
        System.out.println("데이터 개수: " + response.getCount());
        
        if (response.isSuccess() && response.getCount() > 0) {
            ExchangeRateDto rate = response.getData().get(0);
            System.out.println("환율 정보:");
            System.out.println("- 통화코드: " + rate.getCur_unit());
            System.out.println("- 통화명: " + rate.getCur_nm());
            System.out.println("- 매매기준율: " + rate.getDeal_bas_r());
            System.out.println("- TTB: " + rate.getTtb());
            System.out.println("- TTS: " + rate.getTts());
            System.out.println("- 장부가격: " + rate.getBkpr());
            System.out.println("- 년환가료율: " + rate.getYy_efee_r());
            System.out.println("- 10일환가료율: " + rate.getTen_dd_efee_r());
        }
    }
    
    /**
     * API URL 생성 및 연결 테스트
     * API URL이 올바르게 생성되고 실제 연결이 가능한지 확인합니다.
     */
    @Test
    @DisplayName("API URL 생성 및 연결 테스트")
    void testApiConnection() {
        // Given
        System.out.println("=== API URL 및 연결 테스트 ===");
        String expectedBaseUrl = "https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON";
        String testDate = "20250721";
        
        // API URL 출력
        String fullUrl = expectedBaseUrl + "?authkey=Rkp192qK3aGqL8CNbC6SEdRlQfH8R5kp&searchdate=" + testDate + "&data=AP01";
        System.out.println("생성된 API URL: " + fullUrl);
        
        // When - 실제 API 호출
        ExchangeRateResponse response = exchangeRateService.getExchangeRates(testDate);
        
        // Then
        assertNotNull(response, "응답이 null이면 안됩니다");
        System.out.println("연결 테스트 결과: " + (response.isSuccess() ? "성공" : "실패"));
        
        if (!response.isSuccess()) {
            System.out.println("실패 사유: " + response.getMessage());
        }
    }
    
    /**
     * 환율 유틸리티 기능 테스트
     * 날짜 형식 검증, 통화 목록 등 유틸리티 함수들을 테스트합니다.
     */
    @Test
    @DisplayName("환율 유틸리티 기능 테스트")
    void testExchangeRateUtility() {
        // Given
        System.out.println("=== 환율 유틸리티 기능 테스트 ===");
        
        // When & Then
        String currentDate = ExchangeRateUtil.getCurrentDateString();
        String yesterdayDate = ExchangeRateUtil.getYesterdayDateString();
        
        System.out.println("현재 날짜: " + currentDate);
        System.out.println("어제 날짜: " + yesterdayDate);
        
        assertTrue(ExchangeRateUtil.isValidDateFormat(currentDate), "현재 날짜 형식이 유효해야 합니다");
        assertTrue(ExchangeRateUtil.isValidDateFormat(yesterdayDate), "어제 날짜 형식이 유효해야 합니다");
        assertFalse(ExchangeRateUtil.isValidDateFormat("2025-07-21"), "잘못된 날짜 형식은 유효하지 않아야 합니다");
        assertFalse(ExchangeRateUtil.isValidDateFormat("20250732"), "존재하지 않는 날짜는 유효하지 않아야 합니다");
        
        String[] majorCurrencies = ExchangeRateUtil.getMajorCurrencies();
        System.out.println("주요 통화 목록: " + String.join(", ", majorCurrencies));
        assertTrue(majorCurrencies.length > 0, "주요 통화 목록이 있어야 합니다");
    }
}

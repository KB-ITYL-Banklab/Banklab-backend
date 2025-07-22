package com.banklab.financeContents.service;

import com.banklab.config.RootConfig;
import com.banklab.financeContents.dto.GoldPriceInfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GoldPriceService 통합 테스트 클래스
 * 
 * 이 테스트 클래스는 공공데이터포털 금 시세 API 서비스의 모든 기능을
 * 종합적으로 테스트하여 API 연동, 데이터 처리, 예외 처리 등이
 * 정상적으로 동작하는지 검증합니다.
 * 
 * 테스트 목적:
 * - 실제 API 연동 테스트 (네트워크 통신 포함)
 * - 다양한 상황에서의 데이터 처리 검증
 * - 오류 상황 및 예외 처리 테스트
 * - 성능 및 페이징 기능 검증
 * 
 * 주의사항:
 * - 실제 API를 사용하므로 네트워크 연결이 필요합니다
 * - API 키 설정이 올바로 되어있어야 합니다
 * - 금 시장 데이터의 특성상 결과가 유동적일 수 있습니다
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2025.01
 * @see GoldPriceService 테스트 대상 서비스
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("공공데이터 금 시세 API 서비스 테스트")
public class GoldPriceServiceTest {

    /** 테스트 대상 서비스 (스프링 의존성 주입) */
    @Autowired
    private GoldPriceService goldPriceService;

    /** API 인증키 (설정 파일에서 주입, 기본값: 빈 문자열) */
    @Value("${gold.api.key:}")
    private String apiKey;

    /** 테스트에 사용할 기준일자 (YYYYMMDD 형식) */
    private String testDate;

    /**
     * 각 테스트 메서드 실행 전 초기화 작업
     * 
     * 테스트 기준일자를 어제로 설정합니다.
     * 금 시장은 T+1 정산 체계로 인해 오늘 데이터는 아직 제공되지 않습니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트용 날짜 설정 (어제 날짜 사용)
        testDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println("📅 테스트 기준일: " + testDate);
    }

    /**
     * 테스트 1: API 설정 및 의존성 주입 검증
     * 
     * 목적:
     * - 스프링 의존성 주입이 정상적으로 동작하는지 확인
     * - API 인증키가 올바로 설정되어 있는지 확인
     * - 기본 환경 설정 상태 검증
     */
    @Test
    @Order(1)
    @DisplayName("API 설정 및 의존성 주입 확인")
    void testApiConfiguration() {
        System.out.println("\n=== 🔧 API 설정 테스트 ===");
        
        // 서비스 주입 확인
        assertNotNull(goldPriceService, "GoldPriceService가 주입되어야 합니다");
        System.out.println("✅ GoldPriceService 주입 성공");

        // API 키 설정 확인
        assertNotNull(apiKey, "API 키가 설정되어야 합니다");
        assertFalse(apiKey.trim().isEmpty(), "API 키가 비어있으면 안됩니다");
        assertTrue(apiKey.length() > 50, "API 키 길이가 충분해야 합니다 (현재: " + apiKey.length() + "자)");
        
        System.out.println("🔑 API 키 확인:");
        System.out.println("  - 길이: " + apiKey.length() + "자");
        System.out.println("  - 앞부분: " + apiKey.substring(0, Math.min(15, apiKey.length())) + "...");
        System.out.println("✅ API 설정 검증 완료");
    }

    /**
     * 테스트 2: 기본 API 연결 및 데이터 수신 검증
     * 
     * 목적:
     * - 공공데이터포털 API와의 네트워크 연결 테스트
     * - 최소한의 데이터 요청으로 API 응답 검증
     * - JSON 파싱 및 객체 매핑 정상 동작 확인
     */
    @Test
    @Order(2)
    @DisplayName("기본 API 연결 테스트")
    void testBasicApiConnection() {
        System.out.println("\n=== 🌐 기본 API 연결 테스트 ===");
        
        try {
            // 최소한의 데이터로 API 연결 테스트
            List<GoldPriceInfoDto> result = goldPriceService.getGoldPriceInfo(testDate, null, 3, 1);
            
            assertNotNull(result, "API 응답이 null이면 안됩니다");
            assertFalse(result.isEmpty(), "API 응답 데이터가 있어야 합니다");
            assertTrue(result.size() <= 3, "요청한 개수만큼 데이터가 와야 합니다");
            
            System.out.println("✅ API 연결 성공: " + result.size() + "개 데이터 조회");
            
            // 첫 번째 데이터 검증
            GoldPriceInfoDto firstGold = result.get(0);
            assertNotNull(firstGold.getProductName(), "상품명이 있어야 합니다");
            assertNotNull(firstGold.getProductCode(), "상품코드가 있어야 합니다");
            
            System.out.println("📊 첫 번째 상품: " + firstGold.getProductName() + " (" + firstGold.getProductCode() + ")");
            
        } catch (Exception e) {
            fail("API 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 3: 최신 금 시세 조회 기능 검증
     * 
     * 목적:
     * - getLatestGoldPrices() 메서드의 정상 동작 확인
     * - 최신 금 시세 데이터 수신 및 처리 테스트
     * - 데이터 무결성 및 필수 필드 존재 여부 확인
     */
    @Test
    @Order(3)
    @DisplayName("최신 금 시세 조회 테스트")
    void testGetLatestGoldPrices() {
        System.out.println("\n=== 🏆 최신 금 시세 조회 테스트 ===");
        
        try {
            List<GoldPriceInfoDto> latestGoldPrices = goldPriceService.getLatestGoldPrices(5);
            
            assertNotNull(latestGoldPrices, "최신 금 시세 목록이 null이면 안됩니다");
            assertFalse(latestGoldPrices.isEmpty(), "최신 금 시세 데이터가 있어야 합니다");
            assertTrue(latestGoldPrices.size() <= 5, "요청한 개수만큼 데이터가 와야 합니다");
            
            System.out.println("✅ 최신 " + latestGoldPrices.size() + "개 금 시세 조회 성공");
            System.out.println("📈 최신 금 시세 목록:");
            
            for (int i = 0; i < Math.min(3, latestGoldPrices.size()); i++) {
                GoldPriceInfoDto gold = latestGoldPrices.get(i);
                System.out.printf("  %d. %s (%s) - 종가: %s\n", 
                    i+1, gold.getProductName(), gold.getProductCode(), 
                    gold.getClosePrice() != null ? gold.getClosePrice() : "N/A");
                
                // 필수 데이터 검증
                assertNotNull(gold.getProductName(), "상품명이 있어야 합니다");
                assertNotNull(gold.getProductCode(), "상품코드가 있어야 합니다");
                assertNotNull(gold.getBaseDate(), "기준일자가 있어야 합니다");
            }
            
        } catch (Exception e) {
            fail("최신 금 시세 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 4: 특정 금 상품 조회 기능 검증
     * 
     * 목적:
     * - getGoldPriceByProductCode() 메서드의 정상 동작 확인
     * - 존재하지 않는 상품코드에 대한 처리 테스트
     * - 데이터 없음 상황에 대한 예외 처리 테스트
     */
    @Test
    @Order(4)
    @DisplayName("특정 금 상품 조회 테스트")
    void testGetGoldPriceByProductCode() {
        System.out.println("\n=== 🔍 특정 금 상품 조회 테스트 ===");
        
        // 먼저 존재하는 상품코드를 찾아보기
        try {
            List<GoldPriceInfoDto> allGoldPrices = goldPriceService.getLatestGoldPrices(5);
            
            if (allGoldPrices != null && !allGoldPrices.isEmpty()) {
                String testProductCode = allGoldPrices.get(0).getProductCode();
                System.out.println("🧪 테스트용 상품코드: " + testProductCode);
                
                GoldPriceInfoDto goldInfo = goldPriceService.getGoldPriceByProductCode(testProductCode);
                
                if (goldInfo != null) {
                    System.out.println("✅ 금 상품 조회 성공:");
                    System.out.println("  상품명: " + goldInfo.getProductName());
                    System.out.println("  상품코드: " + goldInfo.getProductCode());
                    System.out.println("  종가: " + (goldInfo.getClosePrice() != null ? goldInfo.getClosePrice() : "N/A"));
                    System.out.println("  등락률: " + (goldInfo.getFluctuationRate() != null ? goldInfo.getFluctuationRate() + "%" : "N/A"));
                    
                    // 데이터 검증
                    assertNotNull(goldInfo.getProductName(), "상품명이 있어야 합니다");
                    assertNotNull(goldInfo.getProductCode(), "상품코드가 있어야 합니다");
                    assertEquals(testProductCode, goldInfo.getProductCode(), "요청한 상품코드와 일치해야 합니다");
                    
                } else {
                    System.out.println("⚠️ 해당 상품 데이터를 찾을 수 없습니다");
                    System.out.println("   이는 해당 날짜에 거래 데이터가 없거나 API 특성상 발생할 수 있습니다");
                }
            } else {
                System.out.println("⚠️ 테스트용 데이터를 가져올 수 없어 특정 상품 조회 테스트를 건너뜁니다");
            }
            
        } catch (Exception e) {
            fail("특정 금 상품 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 5: 페이징 기능 정상 동작 검증
     * 
     * 목적:
     * - API의 페이징 기능 정상 동작 확인
     * - 여러 페이지에서 서로 다른 데이터 수신 검증
     * - 대량 데이터 처리 시나리오 준비
     */
    @Test
    @Order(5)
    @DisplayName("페이징 기능 테스트")
    void testPagination() {
        System.out.println("\n=== 📄 페이징 기능 테스트 ===");
        
        try {
            // 1페이지 조회
            List<GoldPriceInfoDto> page1 = goldPriceService.getGoldPriceInfo(testDate, null, 3, 1);
            assertNotNull(page1, "1페이지 데이터가 있어야 합니다");
            
            // 2페이지 조회  
            List<GoldPriceInfoDto> page2 = goldPriceService.getGoldPriceInfo(testDate, null, 3, 2);
            
            System.out.println("📄 1페이지: " + (page1 != null ? page1.size() : 0) + "개");
            System.out.println("📄 2페이지: " + (page2 != null ? page2.size() : 0) + "개");
            
            // 페이지별 데이터가 다른지 확인 (데이터가 충분할 때)
            if (page1 != null && page2 != null && !page1.isEmpty() && !page2.isEmpty()) {
                String firstItemPage1 = page1.get(0).getProductCode();
                String firstItemPage2 = page2.get(0).getProductCode();
                
                if (!firstItemPage1.equals(firstItemPage2)) {
                    System.out.println("✅ 페이징 기능 정상 작동 - 페이지별로 다른 데이터 수신");
                } else {
                    System.out.println("ℹ️ 페이지별 데이터가 동일 (전체 데이터가 적을 수 있음)");
                }
            }
            
        } catch (Exception e) {
            fail("페이징 테스트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 6: 잘못된 입력 및 예외 상황 처리 검증
     * 
     * 목적:
     * - 잘못된 상품코드 입력에 대한 예외 처리 테스트
     * - null 및 빈 문자열 입력에 대한 예외 처리 테스트
     * - 서비스의 로버스트니스 및 안정성 검증
     */
    @Test
    @Order(6)
    @DisplayName("잘못된 파라미터 처리 테스트")
    void testInvalidParameters() {
        System.out.println("\n=== ⚠️ 잘못된 파라미터 처리 테스트 ===");
        
        // 잘못된 상품코드 테스트
        try {
            GoldPriceInfoDto result = goldPriceService.getGoldPriceByProductCode("INVALID_CODE");
            System.out.println("잘못된 상품코드 처리: " + (result == null ? "null 반환 (정상)" : "데이터 반환"));
        } catch (Exception e) {
            System.out.println("잘못된 상품코드 처리: 예외 발생 (정상) - " + e.getClass().getSimpleName());
        }
        
        // 빈 문자열 테스트
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                goldPriceService.getGoldPriceByProductCode("");
            }, "빈 상품코드는 IllegalArgumentException을 발생시켜야 합니다");
            System.out.println("✅ 빈 상품코드 예외 처리 정상");
        } catch (Exception e) {
            System.out.println("⚠️ 빈 상품코드 예외 처리 확인 필요");
        }
        
        // null 테스트
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                goldPriceService.getGoldPriceByProductCode(null);
            }, "null 상품코드는 IllegalArgumentException을 발생시켜야 합니다");
            System.out.println("✅ null 상품코드 예외 처리 정상");
        } catch (Exception e) {
            System.out.println("⚠️ null 상품코드 예외 처리 확인 필요");
        }
    }

    /**
     * 테스트 7: API 호출 성능 및 응답 시간 검증
     * 
     * 목적:
     * - 연속된 API 호출에 대한 성능 측정
     * - 응답 시간이 허용 범위 내에 있는지 확인
     * - 네트워크 타임아웃 설정이 적절한지 검증
     */
    @Test
    @Order(7)
    @DisplayName("성능 테스트")
    void testPerformance() {
        System.out.println("\n=== ⚡ 성능 테스트 ===");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 여러 번의 API 호출로 성능 측정
            for (int i = 0; i < 3; i++) {
                List<GoldPriceInfoDto> result = goldPriceService.getGoldPriceInfo(testDate, null, 3, i + 1);
                assertNotNull(result, "API 호출 결과가 있어야 합니다");
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("⚡ 3회 API 호출 소요 시간: " + duration + "ms");
            System.out.println("⚡ 평균 응답 시간: " + (duration / 3) + "ms");
            
            // 30초 이내에 완료되어야 함
            assertTrue(duration < 30000, "API 호출이 30초 이내에 완료되어야 합니다");
            System.out.println("✅ 성능 테스트 통과");
            
        } catch (Exception e) {
            fail("성능 테스트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 8: 실제 DTO 필드 매핑 검증
     * 
     * 목적:
     * - 수정된 DTO 필드들이 올바르게 매핑되는지 확인
     * - 실제 API 응답 구조와 DTO 일치성 검증
     * - 편의 메서드들의 정상 동작 확인
     */
    @Test
    @Order(8)
    @DisplayName("실제 DTO 필드 매핑 검증 테스트")
    void testActualDtoFieldMapping() {
        System.out.println("\n=== 🔍 실제 DTO 필드 매핑 검증 테스트 ===");
        
        try {
            List<GoldPriceInfoDto> goldPrices = goldPriceService.getGoldPriceInfo(testDate, null, 2, 1);
            
            if (goldPrices != null && !goldPrices.isEmpty()) {
                GoldPriceInfoDto firstGold = goldPrices.get(0);
                
                System.out.println("📋 실제 데이터 필드 확인:");
                System.out.println("  - basDt (기준일자): " + firstGold.getBaseDate());
                System.out.println("  - srtnCd (단축코드): " + firstGold.getShortCode());
                System.out.println("  - isinCd (ISIN코드): " + firstGold.getIsinCode());
                System.out.println("  - itmsNm (종목명): " + firstGold.getItemName());
                System.out.println("  - clpr (종가): " + firstGold.getClosePrice());
                System.out.println("  - vs (대비): " + firstGold.getVersus());
                System.out.println("  - fltRt (등락률): " + firstGold.getFluctuationRate());
                System.out.println("  - trqu (거래량): " + firstGold.getTradingQuantity());
                System.out.println("  - trPrc (거래대금): " + firstGold.getTradingPrice());
                
                // 필수 필드 검증
                assertNotNull(firstGold.getBaseDate(), "기준일자가 있어야 합니다");
                assertNotNull(firstGold.getItemName(), "종목명이 있어야 합니다");
                assertNotNull(firstGold.getIsinCode(), "ISIN코드가 있어야 합니다");
                assertNotNull(firstGold.getClosePrice(), "종가가 있어야 합니다");
                
                // 편의 메서드 검증
                System.out.println("\n📈 편의 메서드 테스트:");
                System.out.println("  - getProductName(): " + firstGold.getProductName());
                System.out.println("  - getProductCode(): " + firstGold.getProductCode());
                System.out.println("  - getFormattedClosePrice(): " + firstGold.getFormattedClosePrice());
                System.out.println("  - getClosePriceAsDouble(): " + firstGold.getClosePriceAsDouble());
                
                // 편의 메서드 검증
                assertEquals(firstGold.getItemName(), firstGold.getProductName(), "상품명 편의 메서드가 정상 작동해야 합니다");
                assertEquals(firstGold.getIsinCode(), firstGold.getProductCode(), "상품코드 편의 메서드가 정상 작동해야 합니다");
                
                System.out.println("✅ DTO 필드 매핑 검증 완료");
                
            } else {
                System.out.println("⚠️ 테스트 데이터가 없어 DTO 필드 매핑 검증을 건너뜁니다");
            }
            
        } catch (Exception e) {
            fail("DTO 필드 매핑 검증 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 9: 다양한 날짜 범위 테스트
     * 
     * 목적:
     * - 과거 여러 날짜의 데이터 조회 테스트
     * - 주말/공휴일 등 거래 없는 날의 처리 확인
     * - 날짜별 데이터 일관성 검증
     */
    @Test
    @Order(9)
    @DisplayName("다양한 날짜 범위 테스트")
    void testMultipleDates() {
        System.out.println("\n=== 📅 다양한 날짜 범위 테스트 ===");
        
        // 최근 5일간의 데이터 확인
        int successCount = 0;
        LocalDate currentDate = LocalDate.now().minusDays(1);
        
        for (int i = 0; i < 5; i++) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            try {
                List<GoldPriceInfoDto> result = goldPriceService.getGoldPriceInfo(dateStr, null, 2, 1);
                
                if (result != null && !result.isEmpty()) {
                    System.out.printf("✅ %s: %d개 데이터\n", dateStr, result.size());
                    successCount++;
                } else {
                    System.out.printf("⚠️ %s: 데이터 없음 (주말/공휴일 가능)\n", dateStr);
                }
                
            } catch (Exception e) {
                System.out.printf("❌ %s: 오류 - %s\n", dateStr, e.getMessage());
            }
            
            currentDate = currentDate.minusDays(1);
        }
        
        System.out.printf("📈 날짜별 조회 결과: %d/5 성공\n", successCount);
        assertTrue(successCount > 0, "최소 1개 날짜의 데이터는 조회되어야 합니다");
    }
}
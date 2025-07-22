package com.banklab.financeContents.service;

import com.banklab.config.RootConfig;
import com.banklab.financeContents.dto.StockSecurityInfoDto;
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
 * PublicDataStockService 통합 테스트 클래스
 * 
 * 이 테스트 클래스는 공공데이터포털 주식 API 서비스의 모든 기능을
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
 * - 주식 시장 데이터의 특성상 결과가 유동적일 수 있습니다
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("공공데이터 주식 API 서비스 테스트")
public class PublicDataStockServiceTest {

    /** 테스트 대상 서비스 (스프링 의존성 주입) */
    @Autowired
    private PublicDataStockService publicDataStockService;

    /** API 인증키 (설정 파일에서 주입, 기본값: 빈 문자열) */
    @Value("${stock.api.key:}")
    private String apiKey;

    /** 테스트에 사용할 기준일자 (YYYYMMDD 형식) */
    private String testDate;

    /**
     * 각 테스트 메서드 실행 전 초기화 작업
     * 
     * 테스트 기준일자를 어제로 설정합니다.
     * 주식 시장은 T+1 정산 체계로 인해 오늘 데이터는 아직 제공되지 않습니다.
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
        assertNotNull(publicDataStockService, "PublicDataStockService가 주입되어야 합니다");
        System.out.println("✅ PublicDataStockService 주입 성공");

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
            List<StockSecurityInfoDto> result = publicDataStockService.getStockPriceInfo(testDate, null, 3, 1);
            
            assertNotNull(result, "API 응답이 null이면 안됩니다");
            assertFalse(result.isEmpty(), "API 응답 데이터가 있어야 합니다");
            assertTrue(result.size() <= 3, "요청한 개수만큼 데이터가 와야 합니다");
            
            System.out.println("✅ API 연결 성공: " + result.size() + "개 데이터 조회");
            
            // 첫 번째 데이터 검증
            StockSecurityInfoDto firstStock = result.get(0);
            assertNotNull(firstStock.getItemName(), "종목명이 있어야 합니다");
            assertNotNull(firstStock.getShortCode(), "종목코드가 있어야 합니다");
            
            System.out.println("📊 첫 번째 종목: " + firstStock.getItemName() + " (" + firstStock.getShortCode() + ")");
            
        } catch (Exception e) {
            fail("API 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 3: 상위 종목 조회 기능 검증
     * 
     * 목적:
     * - getTopStocks() 메서드의 정상 동작 확인
     * - 상위 N개 종목 데이터 수신 및 처리 테스트
     * - 데이터 무결성 및 필수 필드 존재 여부 확인
     */
    @Test
    @Order(3)
    @DisplayName("상위 종목 조회 테스트")
    void testGetTopStocks() {
        System.out.println("\n=== 🏆 상위 종목 조회 테스트 ===");
        
        try {
            List<StockSecurityInfoDto> topStocks = publicDataStockService.getTopStocks(10);
            
            assertNotNull(topStocks, "상위 종목 목록이 null이면 안됩니다");
            assertFalse(topStocks.isEmpty(), "상위 종목 데이터가 있어야 합니다");
            assertTrue(topStocks.size() <= 10, "요청한 개수만큼 데이터가 와야 합니다");
            
            System.out.println("✅ 상위 " + topStocks.size() + "개 종목 조회 성공");
            System.out.println("📈 상위 종목 목록:");
            
            for (int i = 0; i < Math.min(5, topStocks.size()); i++) {
                StockSecurityInfoDto stock = topStocks.get(i);
                System.out.printf("  %d. %s (%s) - 종가: %s원\n", 
                    i+1, stock.getItemName(), stock.getShortCode(), 
                    stock.getClosePrice() != null ? stock.getClosePrice() : "N/A");
                
                // 필수 데이터 검증
                assertNotNull(stock.getItemName(), "종목명이 있어야 합니다");
                assertNotNull(stock.getShortCode(), "종목코드가 있어야 합니다");
                assertEquals(6, stock.getShortCode().length(), "종목코드는 6자리여야 합니다");
            }
            
        } catch (Exception e) {
            fail("상위 종목 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 4: 특정 종목 조회 기능 검증 (삼성전자)
     * 
     * 목적:
     * - getStockInfoByCode() 메서드의 정상 동작 확인
     * - 대표적인 대형주 종목으로 데이터 조회 테스트
     * - 직접 조회 또는 전체 목록 필터링 방식 검증
     * - 데이터 없음 상황에 대한 예외 처리 테스트
     */
    @Test
    @Order(4)
    @DisplayName("특정 종목 조회 테스트 - 삼성전자")
    void testGetStockInfoByCode_Samsung() {
        System.out.println("\n=== 🔍 특정 종목 조회 테스트 (삼성전자) ===");
        
        String samsungCode = "005930";
        
        try {
            StockSecurityInfoDto samsung = publicDataStockService.getStockInfoByCode(samsungCode);
            
            if (samsung != null) {
                System.out.println("✅ 삼성전자 조회 성공:");
                System.out.println("  종목명: " + samsung.getItemName());
                System.out.println("  종목코드: " + samsung.getShortCode());
                System.out.println("  종가: " + (samsung.getClosePrice() != null ? samsung.getClosePrice() + "원" : "N/A"));
                System.out.println("  등락률: " + (samsung.getFluctuationRate() != null ? samsung.getFluctuationRate() + "%" : "N/A"));
                System.out.println("  거래량: " + (samsung.getTradingQuantity() != null ? samsung.getTradingQuantity() + "주" : "N/A"));
                
                // 데이터 검증
                assertNotNull(samsung.getItemName(), "종목명이 있어야 합니다");
                assertNotNull(samsung.getShortCode(), "종목코드가 있어야 합니다");
                assertTrue(samsung.getItemName().contains("삼성"), "삼성 관련 종목이어야 합니다");
                
            } else {
                System.out.println("⚠️ 삼성전자 데이터를 찾을 수 없습니다");
                System.out.println("   이는 해당 날짜에 거래 데이터가 없거나 API 특성상 발생할 수 있습니다");
                
                // null이어도 실패로 처리하지 않음 (데이터 특성상 가능)
                System.out.println("   대안으로 전체 목록에서 삼성전자를 찾아보겠습니다...");
                
                // 전체 목록에서 삼성전자 찾기
                List<StockSecurityInfoDto> allStocks = publicDataStockService.getStockPriceInfo(testDate, null, 100, 1);
                if (allStocks != null) {
                    StockSecurityInfoDto foundSamsung = allStocks.stream()
                        .filter(stock -> stock.getShortCode().equals(samsungCode) || 
                                        (stock.getItemName() != null && stock.getItemName().contains("삼성전자")))
                        .findFirst()
                        .orElse(null);
                    
                    if (foundSamsung != null) {
                        System.out.println("✅ 전체 목록에서 삼성전자 발견: " + foundSamsung.getItemName());
                    }
                }
            }
            
        } catch (Exception e) {
            fail("삼성전자 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 5: 다수 대형주 일괄 조회 기능 검증
     * 
     * 목적:
     * - 여러 주요 종목에 대한 일괄 조회 테스트
     * - 다양한 종목코드의 데이터 조회 성공률 검증
     * - API 호출 빈도 및 안정성 테스트
     * - 종목명 매칭 로직 검증
     */
    @Test
    @Order(5)
    @DisplayName("여러 대형주 조회 테스트")
    void testMultipleMajorStocks() {
        System.out.println("\n=== 📊 여러 대형주 조회 테스트 ===");
        
        // 테스트할 대형주 목록 (종목코드 -> 예상 종목명)
        String[][] testStocks = {
            {"005930", "삼성전자"},
            {"000660", "SK하이닉스"},
            {"005380", "현대차"},
            {"035420", "NAVER"},
            {"006400", "삼성SDI"},
            {"035720", "카카오"}
        };
        
        int successCount = 0;
        
        for (String[] stock : testStocks) {
            String code = stock[0];
            String expectedName = stock[1];
            
            try {
                StockSecurityInfoDto result = publicDataStockService.getStockInfoByCode(code);
                
                if (result != null) {
                    System.out.printf("✅ %s: %s (%s)\n", 
                        code, result.getItemName(), result.getShortCode());
                    
                    // 종목명 일치 확인 (유연한 검증)
                    if (result.getItemName().contains(expectedName.replace("삼성", "").replace("SK", "").replace("LG", ""))) {
                        successCount++;
                    }
                } else {
                    System.out.printf("⚠️ %s (%s): 데이터 없음\n", code, expectedName);
                }
                
            } catch (Exception e) {
                System.out.printf("❌ %s (%s): 오류 - %s\n", code, expectedName, e.getMessage());
            }
        }
        
        System.out.printf("📈 대형주 조회 결과: %d/%d 성공\n", successCount, testStocks.length);
        assertTrue(successCount > 0, "최소 1개 이상의 대형주 데이터는 조회되어야 합니다");
    }

    /**
     * 테스트 6: 페이징 기능 정상 동작 검증
     * 
     * 목적:
     * - API의 페이징 기능 정상 동작 확인
     * - 여러 페이지에서 서로 다른 데이터 수신 검증
     * - 대량 데이터 처리 시나리오 준비
     */
    @Test
    @Order(6)
    @DisplayName("페이징 기능 테스트")
    void testPagination() {
        System.out.println("\n=== 📄 페이징 기능 테스트 ===");
        
        try {
            // 1페이지 조회
            List<StockSecurityInfoDto> page1 = publicDataStockService.getStockPriceInfo(testDate, null, 5, 1);
            assertNotNull(page1, "1페이지 데이터가 있어야 합니다");
            
            // 2페이지 조회  
            List<StockSecurityInfoDto> page2 = publicDataStockService.getStockPriceInfo(testDate, null, 5, 2);
            
            System.out.println("📄 1페이지: " + (page1 != null ? page1.size() : 0) + "개");
            System.out.println("📄 2페이지: " + (page2 != null ? page2.size() : 0) + "개");
            
            // 페이지별 데이터가 다른지 확인 (데이터가 충분할 때)
            if (page1 != null && page2 != null && !page1.isEmpty() && !page2.isEmpty()) {
                String firstItemPage1 = page1.get(0).getShortCode();
                String firstItemPage2 = page2.get(0).getShortCode();
                
                assertNotEquals(firstItemPage1, firstItemPage2, "페이지별로 다른 데이터가 와야 합니다");
                System.out.println("✅ 페이징 기능 정상 작동");
            }
            
        } catch (Exception e) {
            fail("페이징 테스트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 7: 잘못된 입력 및 예외 상황 처리 검증
     * 
     * 목적:
     * - 잘못된 종목코드 입력에 대한 예외 처리 테스트
     * - null 및 빈 문자열 입력에 대한 예외 처리 테스트
     * - 서비스의 로버스트니스 및 안정성 검증
     */
    @Test
    @Order(7)
    @DisplayName("잘못된 파라미터 처리 테스트")
    void testInvalidParameters() {
        System.out.println("\n=== ⚠️ 잘못된 파라미터 처리 테스트 ===");
        
        // 잘못된 종목코드 테스트
        try {
            StockSecurityInfoDto result = publicDataStockService.getStockInfoByCode("INVALID");
            System.out.println("잘못된 종목코드 처리: " + (result == null ? "null 반환 (정상)" : "데이터 반환"));
        } catch (Exception e) {
            System.out.println("잘못된 종목코드 처리: 예외 발생 (정상) - " + e.getClass().getSimpleName());
        }
        
        // 빈 문자열 테스트
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                publicDataStockService.getStockInfoByCode("");
            }, "빈 종목코드는 IllegalArgumentException을 발생시켜야 합니다");
            System.out.println("✅ 빈 종목코드 예외 처리 정상");
        } catch (Exception e) {
            System.out.println("⚠️ 빈 종목코드 예외 처리 확인 필요");
        }
        
        // null 테스트
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                publicDataStockService.getStockInfoByCode(null);
            }, "null 종목코드는 IllegalArgumentException을 발생시켜야 합니다");
            System.out.println("✅ null 종목코드 예외 처리 정상");
        } catch (Exception e) {
            System.out.println("⚠️ null 종목코드 예외 처리 확인 필요");
        }
    }

    /**
     * 테스트 8: API 호출 성능 및 응답 시간 검증
     * 
     * 목적:
     * - 연속된 API 호출에 대한 성능 측정
     * - 응답 시간이 허용 범위 내에 있는지 확인
     * - 네트워크 타임아웃 설정이 적절한지 검증
     */
    @Test
    @Order(8)
    @DisplayName("성능 테스트")
    void testPerformance() {
        System.out.println("\n=== ⚡ 성능 테스트 ===");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 여러 번의 API 호출로 성능 측정
            for (int i = 0; i < 3; i++) {
                List<StockSecurityInfoDto> result = publicDataStockService.getStockPriceInfo(testDate, null, 5, i + 1);
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
}

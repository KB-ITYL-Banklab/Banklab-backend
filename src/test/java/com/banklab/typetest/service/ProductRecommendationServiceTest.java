package com.banklab.typetest.service;

import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.risk.domain.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("유형검사에 따른 추천 상품 서비스 테스트")
class ProductRecommendationServiceTest {

    @Mock
    private ProductRecommendationServiceImpl productRecommendationService;

    @BeforeEach
    void setUp() {
        // 테스트 설정이 필요한 경우 여기에 추가
    }

    @Test
    @DisplayName("안정형 투자성향 - 추천 상품이 올바르게 조회되는지 테스트")
    void 안정형_투자성향에_따른_추천_상품이_올바르게_조회되는지() {
        // Given
        Long investmentTypeId = 1L; // 안정형
        
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(1L)
                        .productName("안전한 정기예금")
                        .riskLevel(RiskLevel.LOW)
                        .interestRate("연 2.5%")
                        .build(),
                RecommendedProductDTO.builder()
                        .productId(2L)
                        .productName("안전한 적금")
                        .riskLevel(RiskLevel.LOW)
                        .interestRate("연 3.0%")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(2, actualProducts.size());
        assertEquals("안전한 정기예금", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.LOW, actualProducts.get(0).getRiskLevel());
        assertEquals("안전한 적금", actualProducts.get(1).getProductName());
        assertEquals(RiskLevel.LOW, actualProducts.get(1).getRiskLevel());
    }

    @Test
    @DisplayName("중립형 투자성향 - 추천 상품이 올바르게 조회되는지 테스트")
    void 중립형_투자성향에_따른_추천_상품이_올바르게_조회되는지() {
        // Given
        Long investmentTypeId = 2L; // 중립형
        
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(3L)
                        .productName("복잡한 적금")
                        .riskLevel(RiskLevel.MEDIUM)
                        .interestRate("연 5.0%")
                        .build(),
                RecommendedProductDTO.builder()
                        .productId(4L)
                        .productName("복잡한 예금")
                        .riskLevel(RiskLevel.MEDIUM)
                        .interestRate("연 6.0%")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(2, actualProducts.size());
        assertEquals("복잡한 적금", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.MEDIUM, actualProducts.get(0).getRiskLevel());
        assertEquals("복잡한 예금", actualProducts.get(1).getProductName());
        assertEquals(RiskLevel.MEDIUM, actualProducts.get(1).getRiskLevel());
    }

    @Test
    @DisplayName("공격형 투자성향 - 추천 상품이 올바르게 조회되는지 테스트")
    void 공격형_투자성향에_따른_추천_상품이_올바르게_조회되는지() {
        // Given
        Long investmentTypeId = 3L; // 공격형
        
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(5L)
                        .productName("개인 신용대출1")
                        .riskLevel(RiskLevel.HIGH)
                        .interestRate("연 10.0%")
                        .build(),
                RecommendedProductDTO.builder()
                        .productId(6L)
                        .productName("개인 신용대출2")
                        .riskLevel(RiskLevel.HIGH)
                        .interestRate("연 12.0%")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(2, actualProducts.size());
        assertEquals("개인 신용대출1", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.HIGH, actualProducts.get(0).getRiskLevel());
        assertEquals("개인 신용대출2", actualProducts.get(1).getProductName());
        assertEquals(RiskLevel.HIGH, actualProducts.get(1).getRiskLevel());
    }

    @Test
    @DisplayName("투자성향 ID를 RiskLevel로 매핑 - 안정형 테스트")
    void 안정형_투자성향_ID를_RiskLevel로_올바르게_매핑하는지() {
        // Given
        Long investmentTypeId = 1L;
        RiskLevel expectedRiskLevel = RiskLevel.LOW;

        when(productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId))
                .thenReturn(expectedRiskLevel);

        // When
        RiskLevel actualRiskLevel = productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId);

        // Then
        assertEquals(RiskLevel.LOW, actualRiskLevel);
    }

    @Test
    @DisplayName("투자성향 ID를 RiskLevel로 매핑 - 중립형 테스트")
    void 중립형_투자성향_ID를_RiskLevel로_올바르게_매핑하는지() {
        // Given
        Long investmentTypeId = 2L;
        RiskLevel expectedRiskLevel = RiskLevel.MEDIUM;

        when(productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId))
                .thenReturn(expectedRiskLevel);

        // When
        RiskLevel actualRiskLevel = productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId);

        // Then
        assertEquals(RiskLevel.MEDIUM, actualRiskLevel);
    }

    @Test
    @DisplayName("투자성향 ID를 RiskLevel로 매핑 - 공격형 테스트")
    void 공격형_투자성향_ID를_RiskLevel로_올바르게_매핑하는지() {
        // Given
        Long investmentTypeId = 3L;
        RiskLevel expectedRiskLevel = RiskLevel.HIGH;

        when(productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId))
                .thenReturn(expectedRiskLevel);

        // When
        RiskLevel actualRiskLevel = productRecommendationService.mapInvestmentTypeToRiskLevel(investmentTypeId);

        // Then
        assertEquals(RiskLevel.HIGH, actualRiskLevel);
    }

    @Test
    @DisplayName("빈 추천 상품 목록 - 해당하는 상품이 없을 때 빈 리스트가 반환되는지 테스트")
    void 해당하는_상품이_없을_때_빈_리스트가_반환되는지() {
        // Given
        Long invalidInvestmentTypeId = 999L;
        
        when(productRecommendationService.getRecommendedProducts(invalidInvestmentTypeId))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(invalidInvestmentTypeId);

        // Then
        assertTrue(actualProducts.isEmpty());
    }
}

package com.banklab.typetest.controller;

import com.banklab.security.util.JwtProcessor;
import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.service.ProductRecommendationService;
import com.banklab.typetest.service.TypeTestService;
import com.banklab.risk.domain.RiskLevel;
import com.banklab.product.domain.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("추천 상품 서비스 테스트")
class ProductRecommendationControllerTest {

    @Mock
    private TypeTestService typeTestService;

    @Mock
    private ProductRecommendationService productRecommendationService;

    @Mock
    private JwtProcessor jwtProcessor;

    @BeforeEach
    void setUp() {
        // 테스트 설정
    }

    @Test
    @DisplayName("안정형 투자성향에 따른 추천 상품 조회 테스트")
    void 안정형_투자성향에_따른_추천_상품_조회_테스트() {
        // Given
        Long investmentTypeId = 1L;
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(1L)
                        .productName("안전한 정기예금")
                        .productType(ProductType.DEPOSIT)
                        .riskLevel(RiskLevel.LOW)
                        .interestRate("연 2.5%")
                        .productFeature("원금 보장되는 안전한 상품입니다.")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(1, actualProducts.size());
        assertEquals("안전한 정기예금", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.LOW, actualProducts.get(0).getRiskLevel());
        assertEquals(ProductType.DEPOSIT, actualProducts.get(0).getProductType());

        verify(productRecommendationService).getRecommendedProducts(investmentTypeId);
    }

    @Test
    @DisplayName("중립형 투자성향에 따른 추천 상품 조회 테스트")
    void 중립형_투자성향에_따른_추천_상품_조회_테스트() {
        // Given
        Long investmentTypeId = 2L;
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(3L)
                        .productName("혼합형 펀드")
                        .productType(ProductType.SAVINGS)
                        .riskLevel(RiskLevel.MEDIUM)
                        .interestRate("연 5.0%")
                        .productFeature("주식과 채권을 균형있게 투자하는 펀드입니다.")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(1, actualProducts.size());
        assertEquals("혼합형 펀드", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.MEDIUM, actualProducts.get(0).getRiskLevel());
        assertEquals(ProductType.SAVINGS, actualProducts.get(0).getProductType());

        verify(productRecommendationService).getRecommendedProducts(investmentTypeId);
    }

    @Test
    @DisplayName("공격형 투자성향에 따른 추천 상품 조회 테스트")
    void 공격형_투자성향에_따른_추천_상품_조회_테스트() {
        // Given
        Long investmentTypeId = 3L;
        List<RecommendedProductDTO> expectedProducts = Arrays.asList(
                RecommendedProductDTO.builder()
                        .productId(5L)
                        .productName("성장형 주식펀드")
                        .productType(ProductType.LOAN)
                        .riskLevel(RiskLevel.HIGH)
                        .interestRate("연 10.0%")
                        .productFeature("고성장 기업에 투자하는 펀드입니다.")
                        .build()
        );

        when(productRecommendationService.getRecommendedProducts(investmentTypeId))
                .thenReturn(expectedProducts);

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(investmentTypeId);

        // Then
        assertEquals(1, actualProducts.size());
        assertEquals("성장형 주식펀드", actualProducts.get(0).getProductName());
        assertEquals(RiskLevel.HIGH, actualProducts.get(0).getRiskLevel());
        assertEquals(ProductType.LOAN, actualProducts.get(0).getProductType());

        verify(productRecommendationService).getRecommendedProducts(investmentTypeId);
    }

    @Test
    @DisplayName("유형검사 결과 조회 테스트")
    void 유형검사_결과_조회_테스트() {
        // Given
        Long memberId = 1L;
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(1L)
                .investmentTypeName("안정형")
                .investmentTypeDesc("원금 보장을 중시하며 안전한 투자를 선호합니다.")
                .build();

        when(typeTestService.getTestResultByUserId(memberId)).thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.getTestResultByUserId(memberId);

        // Then
        assertNotNull(actualResult);
        assertEquals(Long.valueOf(1L), actualResult.getInvestmentTypeId());
        assertEquals("안정형", actualResult.getInvestmentTypeName());

        verify(typeTestService).getTestResultByUserId(memberId);
    }

    @Test
    @DisplayName("결과가 없을 때 null 반환 테스트")
    void 결과가_없을_때_null_반환_테스트() {
        // Given
        Long memberId = 999L;

        when(typeTestService.getTestResultByUserId(memberId)).thenReturn(null);

        // When
        TypeTestResultDTO actualResult = typeTestService.getTestResultByUserId(memberId);

        // Then
        assertNull(actualResult);

        verify(typeTestService).getTestResultByUserId(memberId);
    }

    @Test
    @DisplayName("유효하지 않은 투자성향으로 빈 목록 반환 테스트")
    void 유효하지_않은_투자성향으로_빈_목록_반환_테스트() {
        // Given
        Long invalidInvestmentTypeId = 999L;

        when(productRecommendationService.getRecommendedProducts(invalidInvestmentTypeId))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendedProductDTO> actualProducts = productRecommendationService.getRecommendedProducts(invalidInvestmentTypeId);

        // Then
        assertTrue(actualProducts.isEmpty());

        verify(productRecommendationService).getRecommendedProducts(invalidInvestmentTypeId);
    }

    @Test
    @DisplayName("JWT 토큰에서 memberId 추출 테스트")
    void JWT_토큰에서_memberId_추출_테스트() {
        // Given
        String validToken = "valid-jwt-token";
        Long expectedMemberId = 1L;

        when(jwtProcessor.getMemberId(validToken)).thenReturn(expectedMemberId);

        // When
        Long actualMemberId = jwtProcessor.getMemberId(validToken);

        // Then
        assertEquals(expectedMemberId, actualMemberId);

        verify(jwtProcessor).getMemberId(validToken);
    }

    @Test
    @DisplayName("잘못된 JWT 토큰에서 null 반환 테스트")
    void 잘못된_JWT_토큰에서_null_반환_테스트() {
        // Given
        String invalidToken = "invalid-jwt-token";

        when(jwtProcessor.getMemberId(invalidToken)).thenReturn(null);

        // When
        Long actualMemberId = jwtProcessor.getMemberId(invalidToken);

        // Then
        assertNull(actualMemberId);

        verify(jwtProcessor).getMemberId(invalidToken);
    }
}

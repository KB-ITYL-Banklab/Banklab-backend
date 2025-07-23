package com.banklab.typetest.service;

import com.banklab.product.domain.ProductType;
import com.banklab.risk.domain.ProductRiskRating;
import com.banklab.risk.domain.RiskLevel;
import com.banklab.risk.mapper.ProductRiskRatingMapper;
import com.banklab.typetest.dto.RecommendedProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private final ProductRiskRatingMapper productRiskRatingMapper;

    @Override
    public List<RecommendedProductDTO> getRecommendedProducts(Long investmentTypeId) {
        try {
            log.info("추천상품 조회 시작 - investmentTypeId: {}", investmentTypeId);
            
            // 투자성향을 RiskLevel로 매핑
            RiskLevel riskLevel = mapInvestmentTypeToRiskLevel(investmentTypeId);
            log.info("매핑된 RiskLevel: {}", riskLevel);
            
            // 해당 위험도의 상품들 조회
            List<ProductRiskRating> riskRatings = productRiskRatingMapper.selectByRiskLevel(riskLevel);
            log.info("조회된 상품 개수: {}", riskRatings.size());
            
            for (ProductRiskRating rating : riskRatings) {
                log.info("상품 정보 - ID: {}, 타입: {}, 이름: {}, 회사: {}, 위험도: {}", 
                         rating.getProductId(), rating.getProductType(), 
                         rating.getProductName(), rating.getCompanyName(), rating.getRiskLevel());
            }
            
            // ProductRiskRating을 RecommendedProductDTO로 변환
            List<RecommendedProductDTO> result = riskRatings.stream()
                    .map(this::convertToRecommendedProductDTO)
                    .collect(Collectors.toList());
            
            log.info("변환된 추천상품 개수: {}", result.size());
            return result;
                    
        } catch (Exception e) {
            log.error("추천상품 조회 중 오류 발생: investmentTypeId={}", investmentTypeId, e);
            return List.of(); // 빈 리스트 반환
        }
    }

    @Override
    public RiskLevel mapInvestmentTypeToRiskLevel(Long investmentTypeId) {
        // 투자성향 ID에 따른 RiskLevel 매핑
        return switch (investmentTypeId.intValue()) {
            case 1 -> RiskLevel.LOW;    // 안정형 → LOW
            case 2 -> RiskLevel.MEDIUM; // 중립형 → MEDIUM  
            case 3 -> RiskLevel.HIGH;   // 공격형 → HIGH
            default -> {
                log.warn("알 수 없는 투자성향 ID: {}, 기본값 LOW로 설정", investmentTypeId);
                yield RiskLevel.LOW;
            }
        };
    }
    
    private RecommendedProductDTO convertToRecommendedProductDTO(ProductRiskRating rating) {
        RecommendedProductDTO.RecommendedProductDTOBuilder builder = RecommendedProductDTO.builder()
                .productId(rating.getProductId())
                .productType(rating.getProductType())
                .productName(rating.getProductName())
                .companyName(rating.getCompanyName())
                .riskLevel(rating.getRiskLevel())
                .riskReason(rating.getRiskReason());
        // 상품 타입별 추가 정보 설정
        enrichProductDetails(builder, rating);
        
        return builder.build();
    }
    
    private void enrichProductDetails(RecommendedProductDTO.RecommendedProductDTOBuilder builder, ProductRiskRating rating) {
        try {
            switch (rating.getProductType()) {
                case DEPOSIT -> {
                    builder.productFeature("안전한 예금상품");
                    builder.targetCustomer("안정적인 수익을 원하는 고객");
                    builder.interestRate("2.5~3.5%");
                }
                case SAVINGS -> {
                    builder.productFeature("목돈 마련 적금상품");
                    builder.targetCustomer("꾸준한 저축을 원하는 고객");
                    builder.interestRate("3.0~4.0%");
                }
                case LOAN -> {
                    builder.productFeature("신용대출상품");
                    builder.targetCustomer("자금이 필요한 고객");
                    builder.interestRate("4.5~8.5%");
                }
                default -> {
                    builder.productFeature("금융상품");
                    builder.targetCustomer("일반 고객");
                    builder.interestRate("연 2~5%");
                }
            }
        } catch (Exception e) {
            log.warn("상품 상세정보 조회 중 오류 발생: productId={}, productType={}", 
                     rating.getProductId(), rating.getProductType(), e);
            // 기본값 설정
            builder.productFeature("금융상품");
            builder.targetCustomer("일반 고객");
            builder.interestRate("연 2~5%");
        }
    }
}

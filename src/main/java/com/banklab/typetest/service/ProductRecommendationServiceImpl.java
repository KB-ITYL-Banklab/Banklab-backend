package com.banklab.typetest.service;

import com.banklab.product.dto.ProductRateInfo;
import com.banklab.product.service.ProductRateService;
import com.banklab.product.mapper.DepositProductMapper;
import com.banklab.product.mapper.SavingsProductMapper;
import com.banklab.product.mapper.CreditLoanProductMapper;
import com.banklab.product.domain.DepositProduct;
import com.banklab.product.domain.SavingsProduct;
import com.banklab.product.domain.CreditLoanProduct;
import com.banklab.risk.domain.ProductRiskRating;
import com.banklab.risk.domain.RiskLevel;
import com.banklab.risk.mapper.ProductRiskRatingMapper;
import com.banklab.typetest.dto.RecommendedProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private final ProductRiskRatingMapper productRiskRatingMapper;
    private final ProductRateService productRateService;
    
    // 상품 정보 조회를 위한 Mapper들
    private final DepositProductMapper depositProductMapper;
    private final SavingsProductMapper savingsProductMapper;
    private final CreditLoanProductMapper creditLoanProductMapper;

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

            // 각 상품의 실제 정보를 조회해서 메타데이터 보완
            List<ProductRiskRating> enrichedRatings = enrichProductMetadata(riskRatings);

            // 배치로 모든 상품의 금리 정보 조회
            Map<String, ProductRateInfo> rateInfoMap = productRateService.getBatchProductRates(enrichedRatings);
            log.info("금리 정보 조회 완료: {} 개", rateInfoMap.size());

            // ProductRiskRating을 RecommendedProductDTO로 변환
            List<RecommendedProductDTO> result = enrichedRatings.stream()
                    .map(rating -> convertToRecommendedProductDTO(rating, rateInfoMap))
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
    
    /**
     * 위험도 평가 목록에 실제 상품 정보를 보완
     */
    private List<ProductRiskRating> enrichProductMetadata(List<ProductRiskRating> riskRatings) {
        log.info("상품 메타데이터 보완 시작: {} 개", riskRatings.size());
        
        // 모든 상품을 미리 조회해서 맵으로 만들기
        Map<Long, DepositProduct> depositMap = depositProductMapper.findAllDepositProducts().stream()
                .collect(Collectors.toMap(DepositProduct::getId, p -> p));
        Map<Long, SavingsProduct> savingsMap = savingsProductMapper.findAllSavingsProducts().stream()
                .collect(Collectors.toMap(SavingsProduct::getId, p -> p));
        Map<Long, CreditLoanProduct> loanMap = creditLoanProductMapper.findAllCreditLoanProducts().stream()
                .collect(Collectors.toMap(CreditLoanProduct::getId, p -> p));
        
        log.info("상품 맵 생성 완료 - 예금: {}, 적금: {}, 신용대출: {}", 
                 depositMap.size(), savingsMap.size(), loanMap.size());
        
        for (ProductRiskRating rating : riskRatings) {
            log.info("상품 메타데이터 보완 시도: productType={}, productId={}", 
                     rating.getProductType(), rating.getProductId());
            
            try {
                switch (rating.getProductType()) {
                    case DEPOSIT -> {
                        DepositProduct product = depositMap.get(rating.getProductId());
                        if (product != null) {
                            log.info("예금 상품 조회 성공: {}", product.getFinPrdtNm());
                            rating.setProductName(product.getFinPrdtNm());
                            rating.setCompanyName(product.getKorCoNm());
                            rating.setDclsMonth(product.getDclsMonth());
                            rating.setFinCoNo(product.getFinCoNo());
                            rating.setFinPrdtCd(product.getFinPrdtCd());
                        } else {
                            log.warn("예금 상품 조회 실패: productId={}", rating.getProductId());
                        }
                    }
                    case SAVINGS -> {
                        SavingsProduct product = savingsMap.get(rating.getProductId());
                        if (product != null) {
                            log.info("적금 상품 조회 성공: {}", product.getFinPrdtNm());
                            rating.setProductName(product.getFinPrdtNm());
                            rating.setCompanyName(product.getKorCoNm());
                            rating.setDclsMonth(product.getDclsMonth());
                            rating.setFinCoNo(product.getFinCoNo());
                            rating.setFinPrdtCd(product.getFinPrdtCd());
                        } else {
                            log.warn("적금 상품 조회 실패: productId={}", rating.getProductId());
                        }
                    }
                    case LOAN -> {
                        CreditLoanProduct product = loanMap.get(rating.getProductId());
                        if (product != null) {
                            log.info("신용대출 상품 조회 성공: {}", product.getFinPrdtNm());
                            rating.setProductName(product.getFinPrdtNm());
                            rating.setCompanyName(product.getKorCoNm());
                            rating.setDclsMonth(product.getDclsMonth());
                            rating.setFinCoNo(product.getFinCoNo());
                            rating.setFinPrdtCd(product.getFinPrdtCd());
                        } else {
                            log.warn("신용대출 상품 조회 실패: productId={}", rating.getProductId());
                            log.warn("신용대출 맵에 있는 ID들: {}", loanMap.keySet());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("상품 메타데이터 보완 중 예외 발생: productType={}, productId={}", 
                         rating.getProductType(), rating.getProductId(), e);
            }
        }
        
        log.info("상품 메타데이터 보완 완료");
        return riskRatings;
    }
    
    /**
     * ProductRiskRating과 금리 정보를 RecommendedProductDTO로 변환
     */
    private RecommendedProductDTO convertToRecommendedProductDTO(ProductRiskRating rating, Map<String, ProductRateInfo> rateInfoMap) {
        RecommendedProductDTO.RecommendedProductDTOBuilder builder = RecommendedProductDTO.builder()
                .productId(rating.getProductId())
                .productType(rating.getProductType())
                .productName(rating.getProductName() != null ? rating.getProductName() : getDefaultProductName(rating.getProductType()))
                .companyName(rating.getCompanyName() != null ? rating.getCompanyName() : "은행")
                .riskLevel(rating.getRiskLevel())
                .riskReason(rating.getRiskReason())
                .dclsMonth(rating.getDclsMonth())
                .finCoNo(rating.getFinCoNo())
                .finPrdtCd(rating.getFinPrdtCd());

        // 금리 정보 설정
        String productKey = getProductKey(rating);
        ProductRateInfo rateInfo = rateInfoMap.get(productKey);
        if (rateInfo != null) {
            String interestRate = formatInterestRate(rateInfo.getMinRate(), rateInfo.getMaxRate());
            builder.interestRate(interestRate);
        } else {
            builder.interestRate(getDefaultInterestRate(rating.getProductType()));
        }
        
        return builder.build();
    }
    
    /**
     * 상품 타입별 기본 상품명 반환
     */
    private String getDefaultProductName(com.banklab.product.domain.ProductType productType) {
        return switch (productType) {
            case DEPOSIT -> "예금 상품";
            case SAVINGS -> "적금 상품";
            case LOAN -> "신용대출 상품";
            default -> "금융 상품";
        };
    }
    
    /**
     * 상품 타입별 기본 금리 반환
     */
    private String getDefaultInterestRate(com.banklab.product.domain.ProductType productType) {
        return switch (productType) {
            case DEPOSIT -> "연 2.5~3.5%";
            case SAVINGS -> "연 3.0~4.0%";
            case LOAN -> "연 4.5~8.5%";
            default -> "연 2~5%";
        };
    }
    
    /**
     * BigDecimal 금리를 문자열로 포맷팅
     */
    private String formatInterestRate(BigDecimal minRate, BigDecimal maxRate) {
        if (minRate.equals(maxRate)) {
            return String.format("연 %.2f%%", minRate);
        } else {
            return String.format("연 %.2f~%.2f%%", minRate, maxRate);
        }
    }
    
    /**
     * 상품 키 생성
     */
    private String getProductKey(ProductRiskRating rating) {
        return rating.getDclsMonth() + "_" + rating.getFinCoNo() + "_" + rating.getFinPrdtCd();
    }
}

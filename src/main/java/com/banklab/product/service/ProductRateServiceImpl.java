package com.banklab.product.service;

import com.banklab.product.domain.ProductType;
import com.banklab.product.dto.ProductRateInfo;
import com.banklab.product.dto.deposit.DepositWithOptionsDto;
import com.banklab.product.dto.savings.SavingsWithOptionsDto;
import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;
import com.banklab.product.service.creditloan.CreditLoanDetailService;
import com.banklab.product.service.deposit.DepositDetailService;
import com.banklab.product.service.savings.SavingsDetailService;
import com.banklab.risk.domain.ProductRiskRating;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRateServiceImpl implements ProductRateService {

    private final DepositDetailService depositDetailService;
    private final SavingsDetailService savingsDetailService;
    private final CreditLoanDetailService creditLoanDetailService;

    @Override
    public Map<String, ProductRateInfo> getBatchProductRates(List<ProductRiskRating> ratings) {
        Map<String, ProductRateInfo> rateMap = new HashMap<>();
        
        // 상품 타입별로 그룹화
        Map<ProductType, List<ProductRiskRating>> groupedByType = ratings.stream()
                .collect(Collectors.groupingBy(ProductRiskRating::getProductType));
        
        // 각 타입별로 배치 처리
        groupedByType.forEach((productType, productList) -> {
            switch (productType) {
                case DEPOSIT -> processDepositRates(productList, rateMap);
                case SAVINGS -> processSavingsRates(productList, rateMap);
                case CREDITLOAN -> processLoanRates(productList, rateMap);
                default -> log.warn("지원하지 않는 상품 타입: {}", productType);
            }
        });
        
        return rateMap;
    }
    
    private void processDepositRates(List<ProductRiskRating> deposits, Map<String, ProductRateInfo> rateMap) {
        for (ProductRiskRating rating : deposits) {
            try {
                DepositWithOptionsDto depositInfo = depositDetailService.getDepositWithOptions(
                        rating.getDclsMonth(), rating.getFinCoNo(), rating.getFinPrdtCd());
                
                if (depositInfo != null && depositInfo.getMinRate() != null && depositInfo.getMaxRate() != null) {
                    String key = getProductKey(rating);
                    rateMap.put(key, ProductRateInfo.builder()
                            .dclsMonth(rating.getDclsMonth())
                            .finCoNo(rating.getFinCoNo())
                            .finPrdtCd(rating.getFinPrdtCd())
                            .productType(ProductType.DEPOSIT)
                            .minRate(depositInfo.getMinRate())
                            .maxRate(depositInfo.getMaxRate())
                            .build());
                } else {
                    // 기본값 설정
                    setDefaultRate(rating, rateMap, ProductType.DEPOSIT, 
                                   new BigDecimal("2.5"), new BigDecimal("3.5"));
                }
            } catch (Exception e) {
                log.warn("예금 상품 금리 조회 실패: {}", rating.getFinPrdtCd(), e);
                setDefaultRate(rating, rateMap, ProductType.DEPOSIT, 
                               new BigDecimal("2.5"), new BigDecimal("3.5"));
            }
        }
    }
    
    private void processSavingsRates(List<ProductRiskRating> savings, Map<String, ProductRateInfo> rateMap) {
        for (ProductRiskRating rating : savings) {
            try {
                SavingsWithOptionsDto savingsInfo = savingsDetailService.getSavingsWithOptions(
                        rating.getDclsMonth(), rating.getFinCoNo(), rating.getFinPrdtCd());
                
                if (savingsInfo != null && savingsInfo.getMinRate() != null && savingsInfo.getMaxRate() != null) {
                    String key = getProductKey(rating);
                    rateMap.put(key, ProductRateInfo.builder()
                            .dclsMonth(rating.getDclsMonth())
                            .finCoNo(rating.getFinCoNo())
                            .finPrdtCd(rating.getFinPrdtCd())
                            .productType(ProductType.SAVINGS)
                            .minRate(savingsInfo.getMinRate())
                            .maxRate(savingsInfo.getMaxRate())
                            .build());
                } else {
                    setDefaultRate(rating, rateMap, ProductType.SAVINGS, 
                                   new BigDecimal("3.0"), new BigDecimal("4.0"));
                }
            } catch (Exception e) {
                log.warn("적금 상품 금리 조회 실패: {}", rating.getFinPrdtCd(), e);
                setDefaultRate(rating, rateMap, ProductType.SAVINGS, 
                               new BigDecimal("3.0"), new BigDecimal("4.0"));
            }
        }
    }
    
    private void processLoanRates(List<ProductRiskRating> loans, Map<String, ProductRateInfo> rateMap) {
        log.info("신용대출 상품 금리 처리 시작: {} 개", loans.size());
        
        for (ProductRiskRating rating : loans) {
            log.info("신용대출 상품 정보 - ID: {}, dclsMonth: {}, finCoNo: {}, finPrdtCd: {}", 
                     rating.getProductId(), rating.getDclsMonth(), rating.getFinCoNo(), rating.getFinPrdtCd());
            
            // 신용대출의 경우 메타데이터가 없을 수 있으므로 기본값 사용
            if (rating.getDclsMonth() == null || rating.getFinCoNo() == null || rating.getFinPrdtCd() == null) {
                log.warn("신용대출 상품의 메타데이터가 없어 기본 금리 사용. productId: {}", rating.getProductId());
                setDefaultRate(rating, rateMap, ProductType.CREDITLOAN,
                               new BigDecimal("4.5"), new BigDecimal("8.5"));
                continue;
            }
            
            try {
                log.info("신용대출 상품 조회 시도: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                         rating.getDclsMonth(), rating.getFinCoNo(), rating.getFinPrdtCd());
                
                CreditLoanWithOptionsDto loanInfo = creditLoanDetailService.getCreditLoanWithOptions(
                        rating.getDclsMonth(), rating.getFinCoNo(), rating.getFinPrdtCd());
                
                if (loanInfo != null && loanInfo.getMinRate() != null && loanInfo.getMaxRate() != null) {
                    log.info("신용대출 상품 조회 성공: minRate={}, maxRate={}", loanInfo.getMinRate(), loanInfo.getMaxRate());
                    String key = getProductKey(rating);
                    rateMap.put(key, ProductRateInfo.builder()
                            .dclsMonth(rating.getDclsMonth())
                            .finCoNo(rating.getFinCoNo())
                            .finPrdtCd(rating.getFinPrdtCd())
                            .productType(ProductType.CREDITLOAN)
                            .minRate(loanInfo.getMinRate())
                            .maxRate(loanInfo.getMaxRate())
                            .build());
                } else {
                    log.warn("신용대출 상품 조회 실패 또는 금리 정보 없음: loanInfo={}", loanInfo);
                    setDefaultRate(rating, rateMap, ProductType.CREDITLOAN,
                                   new BigDecimal("4.5"), new BigDecimal("8.5"));
                }
            } catch (Exception e) {
                log.error("대출 상품 금리 조회 중 예외 발생: productId={}, finPrdtCd={}", 
                          rating.getProductId(), rating.getFinPrdtCd(), e);
                setDefaultRate(rating, rateMap, ProductType.CREDITLOAN,
                               new BigDecimal("4.5"), new BigDecimal("8.5"));
            }
        }
    }
    
    private void setDefaultRate(ProductRiskRating rating, Map<String, ProductRateInfo> rateMap, 
                               ProductType productType, BigDecimal minRate, BigDecimal maxRate) {
        String key = getProductKey(rating);
        rateMap.put(key, ProductRateInfo.builder()
                .dclsMonth(rating.getDclsMonth())
                .finCoNo(rating.getFinCoNo())
                .finPrdtCd(rating.getFinPrdtCd())
                .productType(productType)
                .minRate(minRate)
                .maxRate(maxRate)
                .build());
    }
    
    private String getProductKey(ProductRiskRating rating) {
        return rating.getDclsMonth() + "_" + rating.getFinCoNo() + "_" + rating.getFinPrdtCd();
    }
}

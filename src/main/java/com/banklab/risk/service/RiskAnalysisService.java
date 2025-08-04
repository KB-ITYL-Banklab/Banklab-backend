package com.banklab.risk.service;

import com.banklab.product.domain.creditloan.CreditLoanProduct;
import com.banklab.product.domain.deposit.DepositProduct;
import com.banklab.product.domain.savings.SavingsProduct;
import com.banklab.risk.domain.ProductRiskRating;
import com.banklab.product.domain.ProductType;
import com.banklab.risk.mapper.ProductRiskRatingMapper;
import com.banklab.risk.dto.BatchRiskAnalysisRequest;
import com.banklab.risk.dto.RiskAnalysisResponse;
import com.banklab.product.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RiskAnalysisService {
    
    private final BatchClaudeAiAnalysisService batchAiAnalysisService;
    private final ProductRiskRatingMapper productRiskRatingMapper;

    private final DepositProductMapper depositProductMapper;
    private final SavingsProductMapper savingsProductMapper;
    private final CreditLoanProductMapper creditLoanProductMapper;
    
    /**
     * 배치 위험도 분석 - Claude API를 사용한 실제 분석
     */
    public List<ProductRiskRating> batchAnalyzeRisks(List<BatchRiskAnalysisRequest> requests) {
        log.info("배치 위험도 분석 시작: {} 개 상품", requests.size());
        
        try {
            // 한 번(또는 최소한)의 AI API 호출로 모든 상품 분석
            List<RiskAnalysisResponse> analysisResults = batchAiAnalysisService
                .batchAnalyzeProductRisks(requests);

            productRiskRatingMapper.deleteAll();

            List<ProductRiskRating> results = new ArrayList<>();
            for (int i = 0; i < requests.size() && i < analysisResults.size(); i++) {
                try {
                    BatchRiskAnalysisRequest request = requests.get(i);
                    RiskAnalysisResponse analysis = analysisResults.get(i);

                    // 새로운 평가 생성 (기본 정보만)
                    ProductRiskRating riskRating = ProductRiskRating.builder()
                            .productType(request.getProductType())
                            .productId(request.getProductId())
                            .riskLevel(analysis.getRiskLevelEnum())
                            .riskReason(analysis.getRiskReason())
                            .evaluatedAt(LocalDateTime.now())
                            .build();
                    productRiskRatingMapper.insertRiskRating(riskRating);
                    log.info("위험도 평가(신규/갱신) 생성: {} {} -> {}",
                            request.getProductType(), request.getProductId(), analysis.getRiskLevelEnum());

                    results.add(riskRating);

                } catch (Exception e) {
                    log.error("개별 상품 처리 실패: {} {}",
                            requests.get(i).getProductType(), requests.get(i).getProductId(), e);
                }
            }
            log.info("배치 위험도 분석 완료: {}/{} 성공", results.size(), requests.size());
            return results;
            
        } catch (Exception e) {
            log.error("배치 위험도 분석 전체 실패", e);
            throw new RuntimeException("배치 위험도 분석 실패", e);
        }
    }
    
    /**
     * 모든 기존 상품에 대해 배치 위험도 분석 실행
     */
    public void batchAnalyzeAllProductsRisk() {
        log.info("기존 모든 상품에 대한 배치 위험도 분석 시작...");
        
        List<BatchRiskAnalysisRequest> allRequests = new ArrayList<>();
        
        // 예금 상품
        List<DepositProduct> depositProducts = depositProductMapper.findAllDepositProducts();
        for (DepositProduct product : depositProducts) {
            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.DEPOSIT)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd(product.getSpclCnd())
                .mtrtInt(product.getMtrtInt())
                .etcNote(product.getEtcNote())
                .build());
        }
        
        // 적금 상품
        List<SavingsProduct> savingsProducts = savingsProductMapper.findAllSavingsProducts();
        for (SavingsProduct product : savingsProducts) {
            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.SAVINGS)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd(product.getSpclCnd())
                .mtrtInt(product.getMtrtInt())
                .etcNote(product.getEtcNote())
                .build());
        }
        
        // 신용대출 상품
        List<CreditLoanProduct> creditLoanProducts = creditLoanProductMapper.findAllCreditLoanProducts();
        for (CreditLoanProduct product : creditLoanProducts) {
            String etcNote = "대출상품구분: " +
                    (product.getCrdtPrdtTypeNm() != null ? product.getCrdtPrdtTypeNm() : "일반신용대출") +
                    ", 신용평가회사: " +
                    (product.getCbName() != null ? product.getCbName() : "미지정");

            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.LOAN)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("신용대출 상품 - 신용등급별 차등금리 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        
        // 배치 처리 실행
        if (!allRequests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(allRequests);
            log.info("총 {}개 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", allRequests.size(), results.size());
        } else {
            log.warn("분석할 상품이 없습니다.");
        }
    }
}

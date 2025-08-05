package com.banklab.risk.service;

import com.banklab.product.domain.creditloan.CreditLoanProduct;
import com.banklab.product.domain.deposit.DepositProduct;
import com.banklab.product.domain.savings.SavingsProduct;
import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.domain.mortgage.MortgageLoanProduct;
import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
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

import java.time.LocalDate;
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
    private final AnnuityProductMapper annuityProductMapper;
    private final MortgageLoanProductMapper mortgageLoanProductMapper;
    private final RentHouseLoanProductMapper rentHouseLoanProductMapper;
    
    /**
     * 배치 위험도 분석 - Claude API를 사용한 실제 분석
     */
    public List<ProductRiskRating> batchAnalyzeRisks(List<BatchRiskAnalysisRequest> requests) {
        log.info("배치 위험도 분석 시작: {} 개 상품", requests.size());
        
        try {
            // 한 번(또는 최소한)의 AI API 호출로 모든 상품 분석
            List<RiskAnalysisResponse> analysisResults = batchAiAnalysisService
                .batchAnalyzeProductRisks(requests);

            List<ProductRiskRating> results = new ArrayList<>();
            for (int i = 0; i < requests.size() && i < analysisResults.size(); i++) {
                try {
                    BatchRiskAnalysisRequest request = requests.get(i);
                    RiskAnalysisResponse analysis = analysisResults.get(i);

                    // 기존 위험도 평가 삭제 (개별적으로)
                    try {
                        productRiskRatingMapper.deleteByProductTypeAndId(request.getProductType(), request.getProductId());
                        log.debug("기존 위험도 평가 삭제: {} {}", request.getProductType(), request.getProductId());
                    } catch (Exception e) {
                        log.warn("기존 위험도 평가 삭제 실패 (계속 진행): {} {}", request.getProductType(), request.getProductId(), e);
                    }

                    // 새로운 평가 생성
                    ProductRiskRating riskRating = ProductRiskRating.builder()
                            .productType(request.getProductType())
                            .productId(request.getProductId())
                            .riskLevel(analysis.getRiskLevelEnum())
                            .riskReason(analysis.getRiskReason())
                            .evaluatedAt(LocalDateTime.now())
                            .build();
                    
                    productRiskRatingMapper.insertRiskRating(riskRating);
                    log.info("위험도 평가 저장 성공: {} {} -> {} (ID: {})",
                            request.getProductType(), request.getProductId(), 
                            analysis.getRiskLevelEnum(), riskRating.getId());

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
     * 오늘 업데이트된 상품만 대상으로 배치 위험도 분석 실행 (최적화)
     */
    public void batchAnalyzeTodayUpdatedProductsRisk() {
        log.info("오늘 업데이트된 상품에 대한 배치 위험도 분석 시작...");
        
        LocalDate today = LocalDate.now();
        List<BatchRiskAnalysisRequest> todayRequests = new ArrayList<>();
        
        // 오늘 업데이트된 예금 상품
        List<DepositProduct> todayDepositProducts = depositProductMapper.findAllDepositProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (DepositProduct product : todayDepositProducts) {
            todayRequests.add(BatchRiskAnalysisRequest.builder()
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
        log.info("오늘 업데이트된 예금 상품: {}개", todayDepositProducts.size());
        
        // 오늘 업데이트된 적금 상품
        List<SavingsProduct> todaySavingsProducts = savingsProductMapper.findAllSavingsProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (SavingsProduct product : todaySavingsProducts) {
            todayRequests.add(BatchRiskAnalysisRequest.builder()
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
        log.info("오늘 업데이트된 적금 상품: {}개", todaySavingsProducts.size());
        
        // 오늘 업데이트된 신용대출 상품
        List<CreditLoanProduct> todayCreditLoanProducts = creditLoanProductMapper.findAllCreditLoanProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (CreditLoanProduct product : todayCreditLoanProducts) {
            String etcNote = "대출상품구분: " +
                    (product.getCrdtPrdtTypeNm() != null ? product.getCrdtPrdtTypeNm() : "일반신용대출") +
                    ", 신용평가회사: " +
                    (product.getCbName() != null ? product.getCbName() : "미지정");

            todayRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.CREDITLOAN)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("신용대출 상품 - 신용등급별 차등금리 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        log.info("오늘 업데이트된 신용대출 상품: {}개", todayCreditLoanProducts.size());
        
        // 오늘 업데이트된 연금 상품
        List<AnnuityProduct> todayAnnuityProducts = annuityProductMapper.findAllAnnuityProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (AnnuityProduct product : todayAnnuityProducts) {
            String etcNote = String.format("연금종류: %s, 상품유형: %s, 평균수익률: %s%%, 보장수익률: %s%%",
                    product.getPnsnKindNm() != null ? product.getPnsnKindNm() : "미지정",
                    product.getPrdtTypeNm() != null ? product.getPrdtTypeNm() : "미지정",
                    product.getAvgPrftRate() != null ? product.getAvgPrftRate() : "미지정",
                    product.getGuarRate() != null ? product.getGuarRate() : "미지정");

            todayRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.ANNUITY)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd(String.format("연금상품 - 유지기간: %s개월", 
                        product.getMntnCnt() != null ? product.getMntnCnt() : "미지정"))
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        log.info("오늘 업데이트된 연금 상품: {}개", todayAnnuityProducts.size());
        
        // 오늘 업데이트된 주택담보대출 상품
        List<MortgageLoanProduct> todayMortgageProducts = mortgageLoanProductMapper.findAllMortgageLoanProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (MortgageLoanProduct product : todayMortgageProducts) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정", 
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");

            todayRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.MORTGAGE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("주택담보대출 상품 - 담보가치 평가 및 LTV 비율 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        log.info("오늘 업데이트된 주택담보대출 상품: {}개", todayMortgageProducts.size());
        
        // 오늘 업데이트된 전세자금대출 상품
        List<RentHouseLoanProduct> todayRentHouseProducts = rentHouseLoanProductMapper.findAllRentHouseLoanProducts()
                .stream()
                .filter(product -> product.getUpdatedAt() != null && 
                         product.getUpdatedAt().toLocalDate().equals(today))
                .toList();
        
        for (RentHouseLoanProduct product : todayRentHouseProducts) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정", 
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");

            todayRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.RENTHOUSE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("전세자금대출 상품 - 전세보증금 대비 대출비율 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        log.info("오늘 업데이트된 전세자금대출 상품: {}개", todayRentHouseProducts.size());
        
        // 배치 처리 실행
        if (!todayRequests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(todayRequests);
            log.info("총 {}개 상품(오늘 업데이트)에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", 
                    todayRequests.size(), results.size());
        } else {
            log.info("오늘 업데이트된 상품이 없어 위험도 분석을 건너뜁니다.");
        }
    }
    
    /**
     * 모든 기존 상품에 대해 배치 위험도 분석 실행 (기존 메서드는 유지)
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
                .productType(ProductType.CREDITLOAN)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("신용대출 상품 - 신용등급별 차등금리 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        
        // 연금 상품
        List<AnnuityProduct> annuityProducts = annuityProductMapper.findAllAnnuityProducts();
        for (AnnuityProduct product : annuityProducts) {
            String etcNote = String.format("연금종류: %s, 상품유형: %s, 평균수익률: %s%%, 보장수익률: %s%%",
                    product.getPnsnKindNm() != null ? product.getPnsnKindNm() : "미지정",
                    product.getPrdtTypeNm() != null ? product.getPrdtTypeNm() : "미지정",
                    product.getAvgPrftRate() != null ? product.getAvgPrftRate() : "미지정",
                    product.getGuarRate() != null ? product.getGuarRate() : "미지정");

            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.ANNUITY)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd(String.format("연금상품 - 유지기간: %s개월", 
                        product.getMntnCnt() != null ? product.getMntnCnt() : "미지정"))
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        
        // 주택담보대출 상품
        List<MortgageLoanProduct> mortgageProducts = mortgageLoanProductMapper.findAllMortgageLoanProducts();
        for (MortgageLoanProduct product : mortgageProducts) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정", 
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");

            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.MORTGAGE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("주택담보대출 상품 - 담보가치 평가 및 LTV 비율 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        
        // 전세자금대출 상품
        List<RentHouseLoanProduct> rentHouseProducts = rentHouseLoanProductMapper.findAllRentHouseLoanProducts();
        for (RentHouseLoanProduct product : rentHouseProducts) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정", 
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");

            allRequests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.RENTHOUSE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("전세자금대출 상품 - 전세보증금 대비 대출비율 적용")
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

    /**
     * 예금 상품 위험도 분석
     */
    public void batchAnalyzeDepositProductsRisk() {
        log.info("예금 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<DepositProduct> products = depositProductMapper.findAllDepositProducts();
        for (DepositProduct product : products) {
            requests.add(BatchRiskAnalysisRequest.builder()
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
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 예금 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 예금 상품이 없습니다.");
        }
    }

    /**
     * 적금 상품 위험도 분석
     */
    public void batchAnalyzeSavingsProductsRisk() {
        log.info("적금 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<SavingsProduct> products = savingsProductMapper.findAllSavingsProducts();
        for (SavingsProduct product : products) {
            requests.add(BatchRiskAnalysisRequest.builder()
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
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 적금 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 적금 상품이 없습니다.");
        }
    }

    /**
     * 신용대출 상품 위험도 분석
     */
    public void batchAnalyzeCreditLoanProductsRisk() {
        log.info("신용대출 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<CreditLoanProduct> products = creditLoanProductMapper.findAllCreditLoanProducts();
        for (CreditLoanProduct product : products) {
            String etcNote = "대출상품구분: " +
                    (product.getCrdtPrdtTypeNm() != null ? product.getCrdtPrdtTypeNm() : "일반신용대출") +
                    ", 신용평가회사: " +
                    (product.getCbName() != null ? product.getCbName() : "미지정");
            requests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.CREDITLOAN)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("신용대출 상품 - 신용등급별 차등금리 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 신용대출 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 신용대출 상품이 없습니다.");
        }
    }

    /**
     * 연금 상품 위험도 분석
     */
    public void batchAnalyzeAnnuityProductsRisk() {
        log.info("연금 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<AnnuityProduct> products = annuityProductMapper.findAllAnnuityProducts();
        for (AnnuityProduct product : products) {
            String etcNote = String.format("연금종류: %s, 상품유형: %s, 평균수익률: %s%%, 보장수익률: %s%%",
                    product.getPnsnKindNm() != null ? product.getPnsnKindNm() : "미지정",
                    product.getPrdtTypeNm() != null ? product.getPrdtTypeNm() : "미지정",
                    product.getAvgPrftRate() != null ? product.getAvgPrftRate() : "미지정",
                    product.getGuarRate() != null ? product.getGuarRate() : "미지정");
            requests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.ANNUITY)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd(String.format("연금상품 - 유지기간: %s개월",
                        product.getMntnCnt() != null ? product.getMntnCnt() : "미지정"))
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 연금 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 연금 상품이 없습니다.");
        }
    }

    /**
     * 주택담보대출 상품 위험도 분석
     */
    public void batchAnalyzeMortgageLoanProductsRisk() {
        log.info("주택담보대출 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<MortgageLoanProduct> products = mortgageLoanProductMapper.findAllMortgageLoanProducts();
        for (MortgageLoanProduct product : products) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정",
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");
            requests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.MORTGAGE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("주택담보대출 상품 - 담보가치 평가 및 LTV 비율 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 주택담보대출 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 주택담보대출 상품이 없습니다.");
        }
    }

    /**
     * 전세자금대출 상품 위험도 분석
     */
    public void batchAnalyzeRentHouseLoanProductsRisk() {
        log.info("전세자금대출 상품에 대한 배치 위험도 분석 시작...");
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        List<RentHouseLoanProduct> products = rentHouseLoanProductMapper.findAllRentHouseLoanProducts();
        for (RentHouseLoanProduct product : products) {
            String etcNote = String.format("대출부대비용: %s, 중도상환수수료: %s, 연체이자율: %s, 대출한도: %s",
                    product.getLoanInciExpn() != null ? product.getLoanInciExpn() : "미지정",
                    product.getErlyRpayFee() != null ? product.getErlyRpayFee() : "미지정",
                    product.getDlyRate() != null ? product.getDlyRate() : "미지정",
                    product.getLoanLmt() != null ? product.getLoanLmt() : "미지정");
            requests.add(BatchRiskAnalysisRequest.builder()
                .productType(ProductType.RENTHOUSE)
                .productId(product.getId())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .spclCnd("전세자금대출 상품 - 전세보증금 대비 대출비율 적용")
                .mtrtInt("해당없음")
                .etcNote(etcNote)
                .build());
        }
        if (!requests.isEmpty()) {
            List<ProductRiskRating> results = batchAnalyzeRisks(requests);
            log.info("총 {}개 전세자금대출 상품에 대한 배치 위험도 분석이 완료되었습니다. 성공: {}개", requests.size(), results.size());
        } else {
            log.warn("분석할 전세자금대출 상품이 없습니다.");
        }
    }
}

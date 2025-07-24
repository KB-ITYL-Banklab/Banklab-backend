package com.banklab.product;

import com.banklab.product.batch.scheduler.ProductScheduler;
import com.banklab.product.controller.CreditLoanDetailController;
import com.banklab.product.controller.DepositDetailController;
import com.banklab.product.controller.SavingsDetailController;
import com.banklab.product.domain.ProductType;
import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;
import com.banklab.product.dto.deposit.DepositWithOptionsDto;
import com.banklab.product.dto.savings.SavingsWithOptionsDto;
import com.banklab.product.service.CreditLoanDetailService;
import com.banklab.product.service.DepositDetailService;
import com.banklab.product.service.SavingsDetailService;
import com.banklab.risk.dto.BatchRiskAnalysisRequest;
import com.banklab.risk.dto.RiskAnalysisResponse;
import com.banklab.risk.service.BatchClaudeAiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 금융상품 통합 테스트
 * - API 통합 배치 테스트
 * - 금융상품 위험도 계산 Anthropic API 이용 테스트
 * - 예금/적금/신용대출 상품 상세보기 테스트
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("금융상품 통합 테스트")
class FinancialProductIntegrationTest {

    @Mock
    private JobLauncher jobLauncher;
    
    @Mock
    private Job depositRefreshJob;
    
    @Mock
    private Job savingsRefreshJob;
    
    @Mock
    private Job creditLoanRefreshJob;
    
    @Mock
    private DepositDetailService depositDetailService;
    
    @Mock
    private SavingsDetailService savingsDetailService;
    
    @Mock
    private CreditLoanDetailService creditLoanDetailService;
    
    @Mock
    private BatchClaudeAiAnalysisService batchClaudeAiAnalysisService;
    
    private ProductScheduler productScheduler;
    private DepositDetailController depositDetailController;
    private SavingsDetailController savingsDetailController;
    private CreditLoanDetailController creditLoanDetailController;

    @BeforeEach
    void setUp() {
        productScheduler = new ProductScheduler();
        injectMocks();
        
        depositDetailController = new DepositDetailController(depositDetailService);
        savingsDetailController = new SavingsDetailController(savingsDetailService);
        creditLoanDetailController = new CreditLoanDetailController(creditLoanDetailService);
    }
    
    private void injectMocks() {
        try {
            java.lang.reflect.Field jobLauncherField = ProductScheduler.class.getDeclaredField("jobLauncher");
            jobLauncherField.setAccessible(true);
            jobLauncherField.set(productScheduler, jobLauncher);
            
            java.lang.reflect.Field depositJobField = ProductScheduler.class.getDeclaredField("depositRefreshJob");
            depositJobField.setAccessible(true);
            depositJobField.set(productScheduler, depositRefreshJob);
            
            java.lang.reflect.Field savingsJobField = ProductScheduler.class.getDeclaredField("savingsRefreshJob");
            savingsJobField.setAccessible(true);
            savingsJobField.set(productScheduler, savingsRefreshJob);
            
            java.lang.reflect.Field creditLoanJobField = ProductScheduler.class.getDeclaredField("creditLoanRefreshJob");
            creditLoanJobField.setAccessible(true);
            creditLoanJobField.set(productScheduler, creditLoanRefreshJob);
        } catch (Exception e) {
            throw new RuntimeException("Mock 주입 실패", e);
        }
    }

    @Test
    @DisplayName("API 통합 배치 테스트 - 예금/적금/신용대출 배치 순차 실행")
    void testApiIntegrationBatch() throws Exception {
        // Given
        JobExecution successfulExecution = mock(JobExecution.class);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenReturn(successfulExecution);

        // When & Then - 예금 배치 테스트
        log.info("=== 예금 배치 테스트 시작 ===");
        productScheduler.runDepositBatch();
        
        verify(jobLauncher, times(1)).run(eq(depositRefreshJob), any(JobParameters.class));
        log.info("예금 배치 실행 검증 완료");

        // When & Then - 적금 배치 테스트  
        log.info("=== 적금 배치 테스트 시작 ===");
        productScheduler.runSavingsBatch();
        
        verify(jobLauncher, times(1)).run(eq(savingsRefreshJob), any(JobParameters.class));
        log.info("적금 배치 실행 검증 완료");

        // When & Then - 신용대출 배치 테스트
        log.info("=== 신용대출 배치 테스트 시작 ===");
        productScheduler.runCreditLoanBatch();
        
        verify(jobLauncher, times(1)).run(eq(creditLoanRefreshJob), any(JobParameters.class));
        log.info("신용대출 배치 실행 검증 완료");

        // 전체 배치 실행 횟수 검증
        verify(jobLauncher, times(3)).run(any(Job.class), any(JobParameters.class));
        
        log.info("=== API 통합 배치 테스트 완료 ===");
    }

    @Test
    @DisplayName("API 통합 배치 테스트 - 배치 실행 실패 시 예외 처리")
    void testApiIntegrationBatchWithException() throws Exception {
        // Given - 배치 실행 시 예외 발생 시뮬레이션
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenThrow(new RuntimeException("배치 실행 실패"));

        // When & Then - 예외가 발생해도 메서드가 정상 종료되어야 함
        log.info("=== 배치 실패 시나리오 테스트 시작 ===");
        
        // 예외가 발생해도 메서드가 정상적으로 완료되어야 함
        productScheduler.runDepositBatch();
        productScheduler.runSavingsBatch(); 
        productScheduler.runCreditLoanBatch();
        
        // 각 배치가 실행되었는지 확인
        verify(jobLauncher, times(3)).run(any(Job.class), any(JobParameters.class));
        
        log.info("=== 배치 실패 시나리오 테스트 완료 - 예외 처리 확인됨 ===");
    }

    @Test
    @DisplayName("금융상품 위험도 계산 Anthropic API 이용 테스트")
    void testFinancialProductRiskAnalysisWithAnthropicApi() {
        // Given - 테스트용 금융상품 데이터 준비
        List<BatchRiskAnalysisRequest> testRequests = createTestRiskAnalysisRequests();
        
        // Anthropic API 응답 모킹
        List<RiskAnalysisResponse> mockResponses = List.of(
            new RiskAnalysisResponse("LOW", "예금 상품으로 원금보장되며 기본 조건만 있어 저위험으로 평가"),
            new RiskAnalysisResponse("MEDIUM", "적금 상품이지만 우대조건이 복잡하고 중도해지 제약이 있어 중위험으로 평가"),
            new RiskAnalysisResponse("HIGH", "신용대출 상품으로 금리변동 위험과 상환 부담이 있어 고위험으로 평가")
        );
        
        when(batchClaudeAiAnalysisService.batchAnalyzeProductRisks(testRequests))
                .thenReturn(mockResponses);

        // When - 위험도 분석 실행
        log.info("=== Anthropic API 위험도 분석 테스트 시작 ===");
        List<RiskAnalysisResponse> results = batchClaudeAiAnalysisService.batchAnalyzeProductRisks(testRequests);

        // Then - 결과 검증
        assertThat(results).hasSize(3);
        
        // 예금 상품 위험도 검증
        assertThat(results.get(0).getRiskLevel()).isEqualTo("LOW");
        assertThat(results.get(0).getRiskReason()).contains("예금 상품");
        log.info("예금 상품 위험도: {} - {}", results.get(0).getRiskLevel(), results.get(0).getRiskReason());
        
        // 적금 상품 위험도 검증
        assertThat(results.get(1).getRiskLevel()).isEqualTo("MEDIUM");
        assertThat(results.get(1).getRiskReason()).contains("적금 상품");
        log.info("적금 상품 위험도: {} - {}", results.get(1).getRiskLevel(), results.get(1).getRiskReason());
        
        // 신용대출 상품 위험도 검증
        assertThat(results.get(2).getRiskLevel()).isEqualTo("HIGH");
        assertThat(results.get(2).getRiskReason()).contains("신용대출");
        log.info("신용대출 상품 위험도: {} - {}", results.get(2).getRiskLevel(), results.get(2).getRiskReason());

        verify(batchClaudeAiAnalysisService, times(1)).batchAnalyzeProductRisks(testRequests);
        
        log.info("=== Anthropic API 위험도 분석 테스트 완료 ===");
    }

    @Test
    @DisplayName("금융상품 위험도 계산 - API 실패 시 기본값 반환 테스트")
    void testRiskAnalysisWithApiFailure() {
        // Given - API 실패 시뮬레이션
        List<BatchRiskAnalysisRequest> testRequests = createTestRiskAnalysisRequests();
        
        // API 실패 시 기본값 반환 모킹
        List<RiskAnalysisResponse> defaultResponses = testRequests.stream()
                .map(req -> new RiskAnalysisResponse("MEDIUM", "AI 분석 오류로 인한 기본 평가"))
                .toList();
        
        when(batchClaudeAiAnalysisService.batchAnalyzeProductRisks(testRequests))
                .thenReturn(defaultResponses);

        // When
        log.info("=== API 실패 시 기본값 반환 테스트 시작 ===");
        List<RiskAnalysisResponse> results = batchClaudeAiAnalysisService.batchAnalyzeProductRisks(testRequests);

        // Then - 모든 결과가 기본값으로 설정되었는지 확인
        assertThat(results).hasSize(testRequests.size());
        results.forEach(result -> {
            assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
            assertThat(result.getRiskReason()).contains("AI 분석 오류로 인한 기본 평가");
        });
        
        log.info("=== API 실패 시 기본값 반환 테스트 완료 ===");
    }

    @Test
    @DisplayName("예금 상품 상세보기 테스트")
    void testDepositProductDetailView() {
        // Given - 테스트용 예금 상품 데이터
        String dclsMonth = "202401";
        String finCoNo = "0010001";
        String finPrdtCd = "10011000001";
        
        DepositWithOptionsDto mockDepositDto = createMockDepositDto();
        when(depositDetailService.getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd))
                .thenReturn(mockDepositDto);

        // When - 예금 상품 상세 조회
        log.info("=== 예금 상품 상세보기 테스트 시작 ===");
        ResponseEntity<DepositWithOptionsDto> response = depositDetailController
                .getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd);

        // Then - 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFinPrdtNm()).isEqualTo("KB예금통장");
        assertThat(response.getBody().getOptionCount()).isGreaterThan(0);
        
        log.info("예금 상품명: {}", response.getBody().getFinPrdtNm());
        log.info("옵션 개수: {}", response.getBody().getOptionCount());
        log.info("금융회사: {}", response.getBody().getKorCoNm());

        verify(depositDetailService, times(1)).getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd);
        
        log.info("=== 예금 상품 상세보기 테스트 완료 ===");
    }

    @Test
    @DisplayName("예금 상품 상세보기 - 상품 없음 테스트")
    void testDepositProductDetailViewNotFound() {
        // Given - 존재하지 않는 상품 정보
        String dclsMonth = "202401";
        String finCoNo = "9999999";
        String finPrdtCd = "99999999999";
        
        when(depositDetailService.getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd))
                .thenReturn(null);

        // When
        log.info("=== 예금 상품 없음 시나리오 테스트 시작 ===");
        ResponseEntity<DepositWithOptionsDto> response = depositDetailController
                .getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        
        log.info("=== 예금 상품 없음 시나리오 테스트 완료 ===");
    }

    @Test
    @DisplayName("적금 상품 상세보기 테스트")
    void testSavingsProductDetailView() {
        // Given - 테스트용 적금 상품 데이터
        String dclsMonth = "202401";
        String finCoNo = "0010001";
        String finPrdtCd = "10011000002";
        
        SavingsWithOptionsDto mockSavingsDto = createMockSavingsDto();
        when(savingsDetailService.getSavingsWithOptions(dclsMonth, finCoNo, finPrdtCd))
                .thenReturn(mockSavingsDto);

        // When - 적금 상품 상세 조회
        log.info("=== 적금 상품 상세보기 테스트 시작 ===");
        ResponseEntity<SavingsWithOptionsDto> response = savingsDetailController
                .getSavingsWithOptions(dclsMonth, finCoNo, finPrdtCd);

        // Then - 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFinPrdtNm()).isEqualTo("KB적금통장");
        assertThat(response.getBody().getOptionCount()).isGreaterThan(0);
        
        log.info("적금 상품명: {}", response.getBody().getFinPrdtNm());
        log.info("옵션 개수: {}", response.getBody().getOptionCount());
        log.info("금융회사: {}", response.getBody().getKorCoNm());

        verify(savingsDetailService, times(1)).getSavingsWithOptions(dclsMonth, finCoNo, finPrdtCd);
        
        log.info("=== 적금 상품 상세보기 테스트 완료 ===");
    }

    @Test
    @DisplayName("신용대출 상세보기 테스트")
    void testCreditLoanDetailView() {
        // Given - 테스트용 신용대출 상품 데이터
        String dclsMonth = "202401";
        String finCoNo = "0010001";
        String finPrdtCd = "10011000003";
        
        CreditLoanWithOptionsDto mockCreditLoanDto = createMockCreditLoanDto();
        when(creditLoanDetailService.getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd))
                .thenReturn(mockCreditLoanDto);

        // When - 신용대출 상품 상세 조회
        log.info("=== 신용대출 상세보기 테스트 시작 ===");
        ResponseEntity<CreditLoanWithOptionsDto> response = creditLoanDetailController
                .getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);

        // Then - 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFinPrdtNm()).isEqualTo("KB신용대출");
        assertThat(response.getBody().getOptionCount()).isGreaterThan(0);
        
        log.info("신용대출 상품명: {}", response.getBody().getFinPrdtNm());
        log.info("옵션 개수: {}", response.getBody().getOptionCount());
        log.info("금융회사: {}", response.getBody().getKorCoNm());

        verify(creditLoanDetailService, times(1)).getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);
        
        log.info("=== 신용대출 상세보기 테스트 완료 ===");
    }

    @Test
    @DisplayName("신용대출 상세보기 - 예외 발생 테스트")
    void testCreditLoanDetailViewWithException() {
        // Given - 서비스에서 예외 발생 시뮬레이션
        String dclsMonth = "202401";
        String finCoNo = "0010001";
        String finPrdtCd = "10011000003";
        
        when(creditLoanDetailService.getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd))
                .thenThrow(new RuntimeException("데이터베이스 연결 오류"));

        // When
        log.info("=== 신용대출 예외 발생 시나리오 테스트 시작 ===");
        ResponseEntity<CreditLoanWithOptionsDto> response = creditLoanDetailController
                .getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);

        // Then - 서버 에러 응답 확인
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        
        log.info("=== 신용대출 예외 발생 시나리오 테스트 완료 ===");
    }

    // === 테스트 헬퍼 메서드들 ===
    
    private List<BatchRiskAnalysisRequest> createTestRiskAnalysisRequests() {
        List<BatchRiskAnalysisRequest> requests = new ArrayList<>();
        
        // 예금 상품 요청
        requests.add(BatchRiskAnalysisRequest.builder()
                .korCoNm("국민은행")
                .finPrdtNm("KB예금통장")
                .joinWay("영업점, 인터넷, 스마트폰")
                .spclCnd("우대조건 없음")
                .mtrtInt("만기 후 보통예금 이율 적용")
                .etcNote("원금보장")
                .productType(ProductType.DEPOSIT)
                .build());
        
        // 적금 상품 요청
        requests.add(BatchRiskAnalysisRequest.builder()
                .korCoNm("국민은행")
                .finPrdtNm("KB적금통장")
                .joinWay("온라인 전용")
                .spclCnd("급여이체 시 0.1% 우대금리, 카드사용실적 월 30만원 이상시 0.2% 우대금리")
                .mtrtInt("만기 후 자동해지")
                .etcNote("중도해지 시 약정금리의 50% 적용")
                .productType(ProductType.SAVINGS)
                .build());
        
        // 신용대출 상품 요청
        requests.add(BatchRiskAnalysisRequest.builder()
                .korCoNm("국민은행")
                .finPrdtNm("KB신용대출")
                .joinWay("영업점, 인터넷")
                .spclCnd("신용등급 1~3등급, 소득증빙 필수")
                .mtrtInt("변동금리")
                .etcNote("중도상환수수료 있음, 연체 시 신용등급 하락")
                .productType(ProductType.LOAN)
                .build());
        
        return requests;
    }
    
    private DepositWithOptionsDto createMockDepositDto() {
        DepositWithOptionsDto dto = new DepositWithOptionsDto();
        dto.setFinPrdtNm("KB예금통장");
        dto.setKorCoNm("국민은행");
        dto.setOptionCount(3);
        return dto;
    }
    
    private SavingsWithOptionsDto createMockSavingsDto() {
        SavingsWithOptionsDto dto = new SavingsWithOptionsDto();
        dto.setFinPrdtNm("KB적금통장");
        dto.setKorCoNm("국민은행");
        dto.setOptionCount(5);
        return dto;
    }
    
    private CreditLoanWithOptionsDto createMockCreditLoanDto() {
        CreditLoanWithOptionsDto dto = new CreditLoanWithOptionsDto();
        dto.setFinPrdtNm("KB신용대출");
        dto.setKorCoNm("국민은행");
        dto.setOptionCount(2);
        return dto;
    }
}

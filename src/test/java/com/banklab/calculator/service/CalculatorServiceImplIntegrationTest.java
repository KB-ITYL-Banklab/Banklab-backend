package com.banklab.calculator.service;

import com.banklab.calculator.dto.request.DepositCalculateRequest;
import com.banklab.calculator.dto.request.SavingsCalculateRequest;
import com.banklab.calculator.dto.request.LoanCalculateRequest;
import com.banklab.calculator.dto.response.DepositCalculateResponse;
import com.banklab.calculator.dto.response.SavingsCalculateResponse;
import com.banklab.calculator.dto.response.LoanCalculateResponse;
import com.banklab.calculator.domain.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("계산기 서비스 구현체 통합 테스트")
class CalculatorServiceImplIntegrationTest {

    private CalculatorServiceImpl calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorServiceImpl();
    }

    @Test
    @DisplayName("예금 단리 계산 - 1000만원 3.5% 12개월")
    void 예금_단리_계산_실제_로직_테스트() {
        // Given
        DepositCalculateRequest request = new DepositCalculateRequest();
        request.setPrincipal(10_000_000L);
        request.setRate(3.5);
        request.setTermMonths(12);
        request.setIsCompound(false);

        // When
        DepositCalculateResponse response = calculatorService.calculateDeposit(request);

        // Then
        assertNotNull(response);
        assertEquals(10_000_000L, response.getInputConditions().getPrincipal());
        assertEquals(3.5, response.getInputConditions().getRate());
        assertEquals(12, response.getInputConditions().getTermMonths());
        assertEquals(false, response.getInputConditions().getIsCompound());
        assertEquals("단리", response.getInputConditions().getRateType());
        
        // 단리 이자 계산: 10,000,000 * 0.035 * 1 = 350,000
        assertEquals(350_000L, response.getResults().getTotalInterest());
        
        // 세금 계산 확인
        assertNotNull(response.getResults().getGeneralTax());
        assertNotNull(response.getResults().getPreferentialTax());
        assertNotNull(response.getResults().getExemptTax());
        
        // 비과세: 원금 + 이자
        assertEquals(10_350_000L, response.getResults().getExemptTax().getMaturityAmount());
        assertEquals(350_000L, response.getResults().getExemptTax().getAfterTaxInterest());
        assertEquals(0L, response.getResults().getExemptTax().getTaxAmount());
    }

    @Test
    @DisplayName("예금 복리 계산 - 1000만원 3.5% 12개월")
    void 예금_복리_계산_실제_로직_테스트() {
        // Given
        DepositCalculateRequest request = new DepositCalculateRequest();
        request.setPrincipal(10_000_000L);
        request.setRate(3.5);
        request.setTermMonths(12);
        request.setIsCompound(true);

        // When
        DepositCalculateResponse response = calculatorService.calculateDeposit(request);

        // Then
        assertNotNull(response);
        assertEquals(true, response.getInputConditions().getIsCompound());
        assertEquals("복리", response.getInputConditions().getRateType());
        
        // 복리 이자는 단리보다 약간 더 많아야 함
        assertTrue(response.getResults().getTotalInterest() > 350_000L);
        assertTrue(response.getResults().getTotalInterest() < 360_000L); // 대략적인 범위 확인
    }

    @Test
    @DisplayName("적금 만기금액 계산 - 월 50만원 2.5% 24개월 복리")
    void 적금_만기금액_계산_실제_로직_테스트() {
        // Given
        SavingsCalculateRequest request = new SavingsCalculateRequest();
        request.setMonthlyPayment(500_000L);
        request.setRate(2.5);
        request.setTermMonths(24);
        request.setIsCompound(true);

        // When
        SavingsCalculateResponse response = calculatorService.calculateSavings(request);

        // Then
        assertNotNull(response);
        assertEquals(500_000L, response.getInputConditions().getMonthlyPayment());
        assertEquals("MATURITY_AMOUNT", response.getInputConditions().getCalculationType());
        assertEquals("복리", response.getInputConditions().getRateType());
        
        // 총 납입금액
        assertEquals(12_000_000L, response.getSavingsSpecific().getTotalPayment());
        
        // 이자는 0보다 커야 함
        assertTrue(response.getResults().getTotalInterest() > 0);
        
        // 세금 계산 결과가 있어야 함
        assertNotNull(response.getResults());
    }

    @Test
    @DisplayName("적금 목표금액 계산 - 3000만원 목표 2.5% 36개월")
    void 적금_목표금액_계산_실제_로직_테스트() {
        // Given
        SavingsCalculateRequest request = new SavingsCalculateRequest();
        request.setTargetAmount(30_000_000L);
        request.setRate(2.5);
        request.setTermMonths(36);
        request.setIsCompound(true);

        // When
        SavingsCalculateResponse response = calculatorService.calculateSavings(request);

        // Then
        assertNotNull(response);
        assertEquals(30_000_000L, response.getInputConditions().getTargetAmount());
        assertEquals("TARGET_AMOUNT", response.getInputConditions().getCalculationType());
        
        // 필요 월납입금이 계산되어야 함
        assertNotNull(response.getSavingsSpecific().getRequiredMonthlyPaymentGeneral());
        assertNotNull(response.getSavingsSpecific().getRequiredMonthlyPaymentPreferential());
        assertNotNull(response.getSavingsSpecific().getRequiredMonthlyPaymentExempt());
        
        // 일반과세 > 세금우대 > 비과세 순으로 필요금액이 많아야 함
        assertTrue(response.getSavingsSpecific().getRequiredMonthlyPaymentGeneral() > 
                  response.getSavingsSpecific().getRequiredMonthlyPaymentPreferential());
        assertTrue(response.getSavingsSpecific().getRequiredMonthlyPaymentPreferential() > 
                  response.getSavingsSpecific().getRequiredMonthlyPaymentExempt());
        
        // 총납입금은 null이어야 함 (목표금액 모드)
        assertNull(response.getSavingsSpecific().getTotalPayment());
        
        // 세금 계산 결과는 null이어야 함 (목표금액 모드)
        assertNull(response.getResults());
    }

    @Test
    @DisplayName("대출 원리금균등상환 계산 - 1억원 4.5% 36개월")
    void 대출_원리금균등상환_계산_실제_로직_테스트() {
        // Given
        LoanCalculateRequest request = new LoanCalculateRequest();
        request.setLoanAmount(100_000_000L);
        request.setLoanRate(4.5);
        request.setLoanTermMonths(36);
        request.setGracePeriodMonths(0);
        request.setRepaymentMethod(LoanType.EQUAL_PAYMENT);

        // When
        LoanCalculateResponse response = calculatorService.calculateLoan(request);

        // Then
        assertNotNull(response);
        assertEquals(100_000_000L, response.getInputConditions().getLoanAmount());
        assertEquals(LoanType.EQUAL_PAYMENT, response.getInputConditions().getRepaymentMethod());
        assertEquals("원리금균등상환", response.getInputConditions().getRepaymentMethodName());
        
        // 대출 결과 검증
        assertEquals(100_000_000L, response.getResults().getLoanAmount());
        assertTrue(response.getResults().getTotalInterest() > 0);
        assertEquals(response.getResults().getLoanAmount() + response.getResults().getTotalInterest(), 
                    response.getResults().getTotalCost());
        
        // 상환 스케줄이 있어야 함
        assertNotNull(response.getSchedule());
        assertEquals(36, response.getSchedule().size());
        
        // 모든 스케줄 항목이 "상환기간"이어야 함 (거치기간이 없으므로)
        response.getSchedule().forEach(item -> {
            assertEquals("상환기간", item.getPeriod());
            assertTrue(item.getPayment() > 0);
            assertTrue(item.getPrincipal() >= 0);
            assertTrue(item.getInterest() > 0);
        });
    }

    @Test
    @DisplayName("적금 계산 시 잘못된 입력값에 대한 예외 처리")
    void 적금_계산_시_잘못된_입력값에_대한_예외_처리() {
        // Given - 월납입금과 목표금액을 모두 입력한 경우
        SavingsCalculateRequest request = new SavingsCalculateRequest();
        request.setMonthlyPayment(500_000L);
        request.setTargetAmount(12_000_000L);
        request.setRate(2.5);
        request.setTermMonths(24);
        request.setIsCompound(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            calculatorService.calculateSavings(request);
        });
        
        assertEquals("월납입금과 목표금액을 동시에 입력할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("적금 계산 시 필수값이 없는 경우 예외 처리")
    void 적금_계산_시_필수값이_없는_경우_예외_처리() {
        // Given - 월납입금도 목표금액도 없는 경우
        SavingsCalculateRequest request = new SavingsCalculateRequest();
        request.setRate(2.5);
        request.setTermMonths(24);
        request.setIsCompound(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            calculatorService.calculateSavings(request);
        });
        
        assertEquals("월납입금 또는 목표금액 중 하나는 반드시 입력되어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("적금 단리 계산과 복리 계산 차이 확인")
    void 적금_단리와_복리_계산_차이_확인() {
        // Given
        SavingsCalculateRequest simpleRequest = new SavingsCalculateRequest();
        simpleRequest.setMonthlyPayment(100_000L);
        simpleRequest.setRate(3.0);
        simpleRequest.setTermMonths(12);
        simpleRequest.setIsCompound(false);

        SavingsCalculateRequest compoundRequest = new SavingsCalculateRequest();
        compoundRequest.setMonthlyPayment(100_000L);
        compoundRequest.setRate(3.0);
        compoundRequest.setTermMonths(12);
        compoundRequest.setIsCompound(true);

        // When
        SavingsCalculateResponse simpleResponse = calculatorService.calculateSavings(simpleRequest);
        SavingsCalculateResponse compoundResponse = calculatorService.calculateSavings(compoundRequest);

        // Then
        assertEquals("단리", simpleResponse.getInputConditions().getRateType());
        assertEquals("복리", compoundResponse.getInputConditions().getRateType());
        
        // 복리가 단리보다 이자가 더 많아야 함
        assertTrue(compoundResponse.getResults().getTotalInterest() > 
                  simpleResponse.getResults().getTotalInterest());
    }
}

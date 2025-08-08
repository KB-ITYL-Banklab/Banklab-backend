package com.banklab.calculator.service;

import com.banklab.calculator.domain.TaxType;
import com.banklab.calculator.domain.LoanType;
import com.banklab.calculator.dto.request.AnnuityCalculateRequest;
import com.banklab.calculator.dto.request.DepositCalculateRequest;
import com.banklab.calculator.dto.request.SavingsCalculateRequest;
import com.banklab.calculator.dto.request.LoanCalculateRequest;
import com.banklab.calculator.dto.response.AnnuityCalculateResponse;
import com.banklab.calculator.dto.response.DepositCalculateResponse;
import com.banklab.calculator.dto.response.SavingsCalculateResponse;
import com.banklab.calculator.dto.response.LoanCalculateResponse;
import com.banklab.calculator.dto.response.common.TaxCalculationResult;
import com.banklab.calculator.util.TaxCalculationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 금융 계산기 서비스 구현체
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {
    
    @Override
    public DepositCalculateResponse calculateDeposit(DepositCalculateRequest request) {
        Long principal = request.getPrincipal();
        Double yearlyRate = request.getRate() / 100.0;
        Integer termMonths = request.getTermMonths();
        Boolean isCompound = request.getIsCompound();
        
        // 이자 계산
        Long totalInterest;
        if (isCompound) {
            // 복리 계산
            double monthlyRate = yearlyRate / 12;
            double maturityAmount = principal * Math.pow(1 + monthlyRate, termMonths);
            totalInterest = Math.round(maturityAmount - principal);
        } else {
            // 단리 계산
            double yearlyInterest = principal * yearlyRate * (termMonths / 12.0);
            totalInterest = Math.round(yearlyInterest);
        }
        
        // 입력 조건
        DepositCalculateResponse.DepositInputConditions inputConditions = DepositCalculateResponse.DepositInputConditions.builder()
                .principal(principal)
                .rate(request.getRate())
                .termMonths(termMonths)
                .isCompound(isCompound)
                .rateType(isCompound ? "복리" : "단리")
                .build();
        
        // 세금 계산 결과
        TaxCalculationResult results = TaxCalculationUtils.calculateTaxResults(principal, totalInterest);
        
        return DepositCalculateResponse.builder()
                .inputConditions(inputConditions)
                .results(results)
                .build();
    }
    
    @Override
    public SavingsCalculateResponse calculateSavings(SavingsCalculateRequest request) {
        Long monthlyPayment = request.getMonthlyPayment();
        Double yearlyRate = request.getRate() / 100.0;
        Integer termMonths = request.getTermMonths();
        Long targetAmount = request.getTargetAmount();
        Boolean isCompound = request.getIsCompound();
        
        // 입력값 검증
        if ((monthlyPayment == null || monthlyPayment <= 0) && (targetAmount == null || targetAmount <= 0)) {
            throw new IllegalArgumentException("월납입금 또는 목표금액 중 하나는 반드시 입력되어야 합니다.");
        }
        
        if (monthlyPayment != null && monthlyPayment > 0 && targetAmount != null && targetAmount > 0) {
            throw new IllegalArgumentException("월납입금과 목표금액을 동시에 입력할 수 없습니다.");
        }
        
        // 목표금액 계산 모드인지 확인
        boolean isTargetAmountMode = (targetAmount != null && targetAmount > 0);
        
        double monthlyRate = yearlyRate / 12;
        Long totalPayment = null;
        Long totalInterest = null;
        
        // 만기금액 계산 모드
        if (!isTargetAmountMode) {
            totalPayment = monthlyPayment * termMonths;
            
            // 적금 이자 계산
            if (isCompound) {
                // 복리 계산 (선취 방식 - 월초 납입)
                double futureValue = 0;
                for (int i = 1; i <= termMonths; i++) {
                    // 각 월 납입금이 남은 기간만큼 복리로 증가 (선취 방식)
                    int remainingMonths = termMonths - i + 1;
                    double maturedAmount = monthlyPayment * Math.pow(1 + monthlyRate, remainingMonths);
                    futureValue += maturedAmount;
                }
                Long maturityAmount = Math.round(futureValue);
                totalInterest = maturityAmount - totalPayment;
            } else {
                // 단리 계산
                double totalInterestAmount = 0;
                for (int i = 1; i <= termMonths; i++) {
                    int remainingMonths = termMonths - i + 1;
                    double interestForThisPayment = monthlyPayment * yearlyRate * (remainingMonths / 12.0);
                    totalInterestAmount += interestForThisPayment;
                }
                totalInterest = Math.round(totalInterestAmount);
            }
        }
        
        // 목표 금액 달성을 위한 필요 월 납입금 계산
        Long requiredMonthlyPaymentGeneral = null;
        Long requiredMonthlyPaymentPreferential = null;
        Long requiredMonthlyPaymentExempt = null;
        
        // 목표금액이 설정된 경우에만 계산
        if (isTargetAmountMode) {
            if (isCompound) {
                // 복리 기준 계산
                if (monthlyRate == 0) {
                    requiredMonthlyPaymentExempt = targetAmount / termMonths;
                } else {
                    // 비과세 기준 - 선취 방식 (월초 납입)
                    double required = targetAmount / (((Math.pow(1 + monthlyRate, termMonths) - 1) / monthlyRate) * (1 + monthlyRate));
                    requiredMonthlyPaymentExempt = Math.round(required);
                }
                
                // 세금을 고려한 월납입액 계산 (역산)
                requiredMonthlyPaymentGeneral = calculateRequiredPaymentWithTax(targetAmount, monthlyRate, termMonths, TaxType.GENERAL.getRate(), true);
                requiredMonthlyPaymentPreferential = calculateRequiredPaymentWithTax(targetAmount, monthlyRate, termMonths, TaxType.PREFERENTIAL.getRate(), true);
                
            } else {
                // 단리 기준 계산
                // 비과세 기준 월납입액 계산
                requiredMonthlyPaymentExempt = calculateRequiredPaymentSimpleInterest(targetAmount, yearlyRate, termMonths, 0.0);
                
                // 세금을 고려한 월납입액 계산
                requiredMonthlyPaymentGeneral = calculateRequiredPaymentSimpleInterest(targetAmount, yearlyRate, termMonths, TaxType.GENERAL.getRate());
                requiredMonthlyPaymentPreferential = calculateRequiredPaymentSimpleInterest(targetAmount, yearlyRate, termMonths, TaxType.PREFERENTIAL.getRate());
            }
        }
        
        // 입력 조건
        SavingsCalculateResponse.SavingsInputConditions inputConditions = SavingsCalculateResponse.SavingsInputConditions.builder()
                .monthlyPayment(monthlyPayment)
                .rate(request.getRate())
                .termMonths(termMonths)
                .targetAmount(targetAmount)
                .isCompound(isCompound)
                .rateType(isCompound ? "복리" : "단리")
                .calculationType(targetAmount != null && targetAmount > 0 ? "TARGET_AMOUNT" : "MATURITY_AMOUNT")
                .build();
        
        // 적금 전용 결과 (총납입원금은 목표금액 모드에서는 null)
        SavingsCalculateResponse.SavingsSpecificResults savingsSpecific = SavingsCalculateResponse.SavingsSpecificResults.builder()
                .totalPayment(totalPayment)  // 목표금액 모드에서는 null
                .requiredMonthlyPaymentGeneral(requiredMonthlyPaymentGeneral)
                .requiredMonthlyPaymentPreferential(requiredMonthlyPaymentPreferential)
                .requiredMonthlyPaymentExempt(requiredMonthlyPaymentExempt)
                .build();
        
        // 세금 계산 결과 (만기금액 계산 모드에서만 제공)
        TaxCalculationResult results = null;
        if (!isTargetAmountMode) {
            results = TaxCalculationUtils.calculateTaxResults(totalPayment, totalInterest);
        }
        
        return SavingsCalculateResponse.builder()
                .inputConditions(inputConditions)
                .results(results)
                .savingsSpecific(savingsSpecific)
                .build();
    }
    
    @Override
    public LoanCalculateResponse calculateLoan(LoanCalculateRequest request) {
        Long loanAmount = request.getLoanAmount();
        Double yearlyRate = request.getLoanRate() / 100.0;
        Integer loanTermMonths = request.getLoanTermMonths();
        Integer gracePeriodMonths = request.getGracePeriodMonths();
        LoanType repaymentMethod = request.getRepaymentMethod();
        
        double monthlyRate = yearlyRate / 12;
        
        List<LoanCalculateResponse.LoanScheduleItem> schedule = new ArrayList<>();
        Long totalInterest = 0L;
        Long totalPrincipalPayments = 0L;
        Long totalPayments = 0L;
        Integer actualPaymentMonths = loanTermMonths - gracePeriodMonths;
        
        Long balance = loanAmount;
        
        // 거치기간 처리
        for (int i = 1; i <= gracePeriodMonths; i++) {
            Long interestPayment = Math.round(balance * monthlyRate);
            totalInterest += interestPayment;
            totalPayments += interestPayment;
            
            schedule.add(LoanCalculateResponse.LoanScheduleItem.builder()
                    .month(i)
                    .period("거치기간")
                    .payment(interestPayment)
                    .principal(0L)
                    .interest(interestPayment)
                    .balance(balance)
                    .build());
        }
        
        // 상환기간 처리
        if (repaymentMethod == LoanType.EQUAL_PAYMENT) {
            // 원리금균등 상환
            Long monthlyPayment = 0L;
            if (monthlyRate == 0) {
                monthlyPayment = balance / actualPaymentMonths;
            } else {
                // 원리금균등 월납입금 계산 (올림 처리)
                double payment = balance * (monthlyRate * Math.pow(1 + monthlyRate, actualPaymentMonths)) 
                    / (Math.pow(1 + monthlyRate, actualPaymentMonths) - 1);
                monthlyPayment = (long) Math.ceil(payment);
            }
            
            for (int i = gracePeriodMonths + 1; i <= loanTermMonths; i++) {
                Long interestPayment = Math.round(balance * monthlyRate);
                Long principalPayment = monthlyPayment - interestPayment;
                
                // 마지막 달에는 잔액을 모두 정리하여 계산 오차 보정
                if (i == loanTermMonths) {
                    principalPayment = balance;
                    monthlyPayment = principalPayment + interestPayment;
                }
                
                balance -= principalPayment;
                
                totalInterest += interestPayment;
                totalPrincipalPayments += principalPayment;
                totalPayments += monthlyPayment;
                
                schedule.add(LoanCalculateResponse.LoanScheduleItem.builder()
                        .month(i)
                        .period("상환기간")
                        .payment(monthlyPayment)
                        .principal(principalPayment)
                        .interest(interestPayment)
                        .balance(Math.max(balance, 0L))
                        .build());
            }
            
        } else if (repaymentMethod == LoanType.EQUAL_PRINCIPAL) {
            // 원금균등 상환
            Long monthlyPrincipalPayment = balance / actualPaymentMonths;
            Long remainingPrincipal = balance; // 남은 원금 추적
            
            for (int i = gracePeriodMonths + 1; i <= loanTermMonths; i++) {
                Long currentPrincipalPayment;
                Long interestPayment;
                
                if (i == loanTermMonths) {
                    // 마지막 달: 남은 잔액을 모두 상환하여 오차 보정
                    currentPrincipalPayment = remainingPrincipal;
                    interestPayment = Math.round(remainingPrincipal * monthlyRate);
                } else {
                    currentPrincipalPayment = monthlyPrincipalPayment;
                    interestPayment = Math.round(remainingPrincipal * monthlyRate);
                }
                
                Long payment = currentPrincipalPayment + interestPayment;
                remainingPrincipal -= currentPrincipalPayment;
                
                totalInterest += interestPayment;
                totalPrincipalPayments += currentPrincipalPayment;
                totalPayments += payment;
                
                schedule.add(LoanCalculateResponse.LoanScheduleItem.builder()
                        .month(i)
                        .period("상환기간")
                        .payment(payment)
                        .principal(currentPrincipalPayment)
                        .interest(interestPayment)
                        .balance(Math.max(remainingPrincipal, 0L))
                        .build());
            }
            
        } else if (repaymentMethod == LoanType.BULLET_PAYMENT) {
            // 원금만기일시상환
            double exactMonthlyInterest = balance * monthlyRate;
            Long targetTotalInterest = Math.round(exactMonthlyInterest * loanTermMonths);
            Long monthlyInterestPayment = Math.round(exactMonthlyInterest);
            
            for (int i = gracePeriodMonths + 1; i < loanTermMonths; i++) {
                totalInterest += monthlyInterestPayment;
                totalPayments += monthlyInterestPayment;
                
                schedule.add(LoanCalculateResponse.LoanScheduleItem.builder()
                        .month(i)
                        .period("상환기간")
                        .payment(monthlyInterestPayment)
                        .principal(0L)
                        .interest(monthlyInterestPayment)
                        .balance(balance)
                        .build());
            }
            
            // 마지막 달에 원금 + 이자 일시상환 (총이자 목표에 맞춰 조정)
            Long lastInterestPayment = targetTotalInterest - totalInterest;
            Long lastPayment = balance + lastInterestPayment;
            totalInterest += lastInterestPayment;
            totalPrincipalPayments += balance;
            totalPayments += lastPayment;
            
            schedule.add(LoanCalculateResponse.LoanScheduleItem.builder()
                    .month(loanTermMonths)
                    .period("상환기간")
                    .payment(lastPayment)
                    .principal(balance)
                    .interest(lastInterestPayment)
                    .balance(0L)
                    .build());
        }
        
        // 평균 계산 - 전체 대출기간 기준으로 계산
        Long avgMonthlyPrincipal = loanTermMonths > 0 ? totalPrincipalPayments / loanTermMonths : 0L;
        Long avgMonthlyInterest = loanTermMonths > 0 ? totalInterest / loanTermMonths : 0L;
        Long avgMonthlyPayment = loanTermMonths > 0 ? totalPayments / loanTermMonths : 0L;
        
        // 입력 조건
        LoanCalculateResponse.InputConditions inputConditions = LoanCalculateResponse.InputConditions.builder()
                .loanAmount(loanAmount)
                .loanTermMonths(loanTermMonths)
                .loanRate(request.getLoanRate())
                .gracePeriodMonths(gracePeriodMonths)
                .repaymentMethod(repaymentMethod)
                .repaymentMethodName(repaymentMethod.getDescription())
                .build();
        
        // 계산 결과
        LoanCalculateResponse.CalculationResults results = LoanCalculateResponse.CalculationResults.builder()
                .loanAmount(loanAmount)
                .totalInterest(totalInterest)
                .totalCost(loanAmount + totalInterest)
                .avgMonthlyPrincipal(avgMonthlyPrincipal)
                .avgMonthlyInterest(avgMonthlyInterest)
                .avgMonthlyPayment(avgMonthlyPayment)
                .build();
        
        return LoanCalculateResponse.builder()
                .inputConditions(inputConditions)
                .results(results)
                .schedule(schedule)
                .build();
    }
    
    /**
     * 단리 기준으로 목표금액 달성을 위한 월납입액 계산
     */
    private Long calculateRequiredPaymentSimpleInterest(Long targetAmount, Double yearlyRate, Integer termMonths, Double taxRate) {
        // 역산을 위한 반복 계산
        double estimatedPayment = targetAmount / termMonths;
        
        for (int iteration = 0; iteration < 20; iteration++) {
            // 추정 월납입액으로 이자 계산
            double totalInterestForEstimate = 0;
            for (int i = 1; i <= termMonths; i++) {
                int remainingMonths = termMonths - i + 1;
                double interestForThisPayment = estimatedPayment * yearlyRate * (remainingMonths / 12.0);
                totalInterestForEstimate += interestForThisPayment;
            }
            
            // 세금 계산
            double taxAmount = totalInterestForEstimate * taxRate;
            double afterTaxInterest = totalInterestForEstimate - taxAmount;
            
            // 예상 만기금액 (총납입금 + 세후이자)
            double estimatedTotal = estimatedPayment * termMonths + afterTaxInterest;
            
            // 목표금액과 비교하여 수렴 확인
            if (Math.abs(estimatedTotal - targetAmount) < 1) break;
            
            // 다음 추정값 계산
            estimatedPayment = estimatedPayment * targetAmount / estimatedTotal;
        }
        
        return Math.round(estimatedPayment);
    }
    
    /**
     * 복리 기준으로 목표금액 달성을 위한 월납입액 계산
     */
    private Long calculateRequiredPaymentWithTax(Long targetAmount, Double monthlyRate, Integer termMonths, Double taxRate, Boolean isCompound) {
        // 역산을 위한 반복 계산
        double estimatedPayment = targetAmount / termMonths;
        
        for (int iteration = 0; iteration < 20; iteration++) {
            // 추정 월납입액으로 복리 계산 (선취 방식)
            double futureValue = 0;
            for (int i = 1; i <= termMonths; i++) {
                int remainingMonths = termMonths - i + 1;
                double maturedAmount = estimatedPayment * Math.pow(1 + monthlyRate, remainingMonths);
                futureValue += maturedAmount;
            }
            
            // 이자 및 세금 계산
            double totalPayment = estimatedPayment * termMonths;
            double totalInterest = futureValue - totalPayment;
            double taxAmount = totalInterest * taxRate;
            double afterTaxInterest = totalInterest - taxAmount;
            
            // 예상 만기금액 (총납입금 + 세후이자)
            double estimatedTotal = totalPayment + afterTaxInterest;
            
            // 목표금액과 비교하여 수렴 확인
            if (Math.abs(estimatedTotal - targetAmount) < 1) break;
            
            // 다음 추정값 계산
            estimatedPayment = estimatedPayment * targetAmount / estimatedTotal;
        }
        
        return Math.round(estimatedPayment);
    }
    @Override
    public AnnuityCalculateResponse calculateAnnuity(AnnuityCalculateRequest request) {
        Long monthlySaving = request.getMonthlySaving();    // 월 적립
        Long lumpSum = request.getLumpSum();                // 거치금
        Integer savingYears = request.getSavingYears();     // 투자 기간 (년)
        Double rate = request.getRate() / 100.0;            // 연이율 (소수로 변환)
        Integer paymentYears = request.getPaymentYears();   // 연금 수령 기간 (년)

        double monthlyRate = rate / 12.0;
        int totalSavingMonths = savingYears * 12;

        // 1. 월 적립금 복리 계산 (선취 기준, 월초 납입)
        double savingFV = 0;
        for (int i = 1; i <= totalSavingMonths; i++) {
            int remainingMonths = totalSavingMonths - i + 1;
            savingFV += monthlySaving * Math.pow(1 + monthlyRate, remainingMonths);
        }

        // 2. 거치금 복리 계산
        double lumpSumFV = lumpSum * Math.pow(1 + monthlyRate, totalSavingMonths);

        // 3. 총 만기 금액 (세전)
        double totalPrincipal = savingFV + lumpSumFV;

        // 4. 총 투자 원금
        double totalInvested = monthlySaving * totalSavingMonths + lumpSum;

        // 5. 이자 수익
        double interestEarned = totalPrincipal - totalInvested;

        // 6. 세금 계산 (일반과세 15.4%)
        double taxAmount = interestEarned * TaxType.GENERAL.getRate();

        // 7. 세후 만기 금액
        double principalAfterTax = totalPrincipal - taxAmount;

        // 8. 연금 수령 개월 수
        int paymentMonths = paymentYears * 12;

        // 9. 월 수령액 계산 (월복리 연금 지급 공식)
        double monthlyPayout = principalAfterTax * (monthlyRate / (1 - Math.pow(1 + monthlyRate, -paymentMonths)));
        double totalPayout = monthlyPayout * paymentMonths;

        // 10. 응답 생성
        AnnuityCalculateResponse.InputConditions inputConditions =
                AnnuityCalculateResponse.InputConditions.builder()
                        .monthlySaving(monthlySaving)
                        .lumpSum(lumpSum)
                        .savingYears(savingYears)
                        .rate(request.getRate())
                        .paymentYears(paymentYears)
                        .build();

        AnnuityCalculateResponse.AnnuityResults results =
                AnnuityCalculateResponse.AnnuityResults.builder()
                        .totalPayout(Math.round(totalPayout))
                        .totalMonths(paymentMonths)
                        .monthlyPayout(Math.round(monthlyPayout))
                        .build();

        return AnnuityCalculateResponse.builder()
                .inputConditions(inputConditions)
                .results(results)
                .build();
    }


}

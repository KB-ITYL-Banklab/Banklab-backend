package com.banklab.calculator.service;

import com.banklab.calculator.dto.request.DepositCalculateRequest;
import com.banklab.calculator.dto.request.SavingsCalculateRequest;
import com.banklab.calculator.dto.request.LoanCalculateRequest;
import com.banklab.calculator.dto.response.DepositCalculateResponse;
import com.banklab.calculator.dto.response.SavingsCalculateResponse;
import com.banklab.calculator.dto.response.LoanCalculateResponse;

/**
 * 금융 계산기 서비스 인터페이스
 */
public interface CalculatorService {
    
    /**
     * 예금 계산
     */
    DepositCalculateResponse calculateDeposit(DepositCalculateRequest request);
    
    /**
     * 적금 계산
     */
    SavingsCalculateResponse calculateSavings(SavingsCalculateRequest request);
    
    /**
     * 대출 계산
     */
    LoanCalculateResponse calculateLoan(LoanCalculateRequest request);
}

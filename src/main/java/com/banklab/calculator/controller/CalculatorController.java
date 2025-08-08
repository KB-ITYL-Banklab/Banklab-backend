package com.banklab.calculator.controller;

import com.banklab.calculator.dto.request.AnnuityCalculateRequest;
import com.banklab.calculator.dto.request.DepositCalculateRequest;
import com.banklab.calculator.dto.request.SavingsCalculateRequest;
import com.banklab.calculator.dto.request.LoanCalculateRequest;
import com.banklab.calculator.dto.response.*;
import com.banklab.calculator.service.CalculatorService;
import com.banklab.calculator.service.UserProfileService;

import com.banklab.security.util.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 금융 계산기 API 컨트롤러
 */
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
@Slf4j
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final UserProfileService userProfileService;
    private final LoginUserProvider loginUserProvider;

    /**
     * 예금 계산기 - 단리, 복리 옵션 존재
     * @param request
     * @return
     */
    @PostMapping("/deposit")
    public ResponseEntity<DepositCalculateResponse> calculateDeposit(
            @RequestBody DepositCalculateRequest request) {

        log.info("예금 계산 요청: 원금={}, 금리={}%, 기간={}개월, 복리={}",
                request.getPrincipal(), request.getRate(), request.getTermMonths(), request.getIsCompound());

        DepositCalculateResponse response = calculatorService.calculateDeposit(request);

        log.info("예금 계산 결과: 총이자={}", response.getResults().getTotalInterest());

        return ResponseEntity.ok(response);
    }

    /**
     * 적금 계산기
     * 만기 - 단리, 복리
     * 목표금액 - 단리, 복리
     * @param request
     * @return
     */
    @PostMapping("/savings")
    public ResponseEntity<SavingsCalculateResponse> calculateSavings(
            @RequestBody SavingsCalculateRequest request) {

        log.info("적금 계산 요청: 월납입={}원, 금리={}%, 기간={}개월, 목표금액={}",
                request.getMonthlyPayment(), request.getRate(), request.getTermMonths(), request.getTargetAmount());

        SavingsCalculateResponse response = calculatorService.calculateSavings(request);

        if (response.getResults() != null) {
            log.info("적금 계산 결과: 총이자={}", response.getResults().getTotalInterest());
        } else {
            log.info("적금 목표금액 계산 완료: 일반과세={}원, 세금우대={}원, 비과세={}원", 
                    response.getSavingsSpecific().getRequiredMonthlyPaymentGeneral(),
                    response.getSavingsSpecific().getRequiredMonthlyPaymentPreferential(),
                    response.getSavingsSpecific().getRequiredMonthlyPaymentExempt());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 대출 계산기
     * 원리금 균등 상환, 원금 균등 상환, 원금만기일시상환
     * @param request
     * @return
     */
    @PostMapping("/loan")
    public ResponseEntity<LoanCalculateResponse> calculateLoan(
            @RequestBody LoanCalculateRequest request) {

        log.info("대출 계산 요청: 대출금액={}원, 금리={}%, 기간={}개월, 상환방식={}",
                request.getLoanAmount(), request.getLoanRate(), request.getLoanTermMonths(), request.getRepaymentMethod());

        LoanCalculateResponse response = calculatorService.calculateLoan(request);

        log.info("대출 계산 결과: 총비용={}, 총이자={}", response.getResults().getTotalCost(), response.getResults().getTotalInterest());

        return ResponseEntity.ok(response);
    }

    /**
     * 연금 계산기
     * @param request
     * @return
     */
    @PostMapping("/annuity")
    public ResponseEntity<AnnuityCalculateResponse> calculateAnnuity(
            @RequestBody AnnuityCalculateRequest request) {

        AnnuityCalculateResponse response = calculatorService.calculateAnnuity(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 투자 프로필 조회 (계산기용)
     * JWT 토큰에서 사용자 ID를 추출하여 프로필 조회
     * @return 투자 프로필 정보
     */
    @GetMapping("/profile")
    public ResponseEntity<UserInvestmentProfileResponse> getUserProfile() {
        Long memberId = loginUserProvider.getLoginMemberId();

        log.info("사용자 투자 프로필 조회 요청: memberId={}", memberId);
        UserInvestmentProfileResponse response = userProfileService.getUserInvestmentProfile(memberId);
        return ResponseEntity.ok(response);
    }
}

package com.banklab.calculator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 투자 프로필 응답 DTO (계산기용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvestmentProfileResponse {
    
    /**
     * 사용자 프로필 기반 기본 거치금액 (원)
     */
    private Long defaultAmount;
    
    /**
     * 사용자 프로필 기반 기본 거치기간 (개월)
     */
    private Integer defaultTermMonths;
}

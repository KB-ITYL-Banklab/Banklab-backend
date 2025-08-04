package com.banklab.calculator.service;

import com.banklab.calculator.dto.response.UserInvestmentProfileResponse;

/**
 * 사용자 프로필 서비스 인터페이스
 */
public interface UserProfileService {
    
    /**
     * 사용자 투자 프로필 조회 (계산기용)
     * @param userId 사용자 ID
     * @return 투자 프로필 정보
     */
    UserInvestmentProfileResponse getUserInvestmentProfile(Long userId);
}

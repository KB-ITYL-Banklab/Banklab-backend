package com.banklab.calculator.service;

import com.banklab.calculator.dto.response.UserInvestmentProfileResponse;
import com.banklab.calculator.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 사용자 프로필 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    
    private final UserProfileMapper userProfileMapper;

    private static final long DEFAULT_AMOUNT = 3_000_000L;
    private static final long OVER_500_AMOUNT = 5_000_000L;
    private static final long UNDER_500_AMOUNT = 1_000_000L;
    private static final int DEFAULT_TERM_MONTHS = 12;
    private static final int SHORT_TERM_MONTHS = 6;
    private static final int LONG_TERM_MONTHS = 24;
    
    @Override
    public UserInvestmentProfileResponse getUserInvestmentProfile(Long userId) {
        log.info("사용자 투자 프로필 조회 요청: userId={}", userId);
        
        // DB에서 사용자 투자 프로필 조회
        Map<String, String> profileInfo = userProfileMapper.findUserInvestmentInfoByUserId(userId);
        
        if (profileInfo == null || profileInfo.isEmpty()) {
            log.warn("사용자 투자 프로필을 찾을 수 없습니다: userId={}", userId);
            throw new RuntimeException("사용자 투자 프로필을 찾을 수 없습니다.");
        }
        
        // DB 값을 실제 기본값으로 변환
        Long defaultAmount = convertToDefaultAmount(profileInfo.get("amountRange"));
        Integer defaultTermMonths = convertToDefaultTermMonths(profileInfo.get("preferredTerm"));
        
        UserInvestmentProfileResponse response = UserInvestmentProfileResponse.builder()
                .defaultAmount(defaultAmount)
                .defaultTermMonths(defaultTermMonths)
                .build();
        
        log.info("사용자 투자 프로필 조회 결과: defaultAmount={}, defaultTermMonths={}", 
                response.getDefaultAmount(), response.getDefaultTermMonths());
        
        return response;
    }
    
    /**
     * DB의 available_amount_range를 실제 기본 금액으로 변환
     * @param dbValue DB에 저장된 enum 값 (UNDER_500, OVER_500)
     * @return 실제 기본 금액 (원)
     */
    private Long convertToDefaultAmount(String dbValue) {
        if (dbValue == null) {
            return DEFAULT_AMOUNT; // 기본값: 100만원
        }
        
        switch (dbValue.toUpperCase()) {
            case "OVER_500":
                return OVER_500_AMOUNT; // 500만원
            case "UNDER_500":
            default:
                return UNDER_500_AMOUNT; // 100만원
        }
    }
    
    /**
     * DB의 investment_period_range를 실제 기본 기간으로 변환
     * @param dbValue DB에 저장된 enum 값 (SHORT_TERM, LONG_TERM)
     * @return 실제 기본 기간 (개월)
     */
    private Integer convertToDefaultTermMonths(String dbValue) {
        if (dbValue == null) {
            return DEFAULT_TERM_MONTHS; // 기본값: 6개월
        }
        
        switch (dbValue.toUpperCase()) {
            case "LONG_TERM":
                return LONG_TERM_MONTHS; // 24개월 (2년)
            case "SHORT_TERM":
            default:
                return SHORT_TERM_MONTHS; // 6개월
        }
    }
}

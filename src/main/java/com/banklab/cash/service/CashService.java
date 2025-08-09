package com.banklab.cash.service;

import com.banklab.cash.dto.CashDTO;

public interface CashService {

    /**
     * 회원의 현금 정보 조회 (없으면 0원으로 초기화 후 반환)
     * @param memberId 회원 ID
     * @return 현금 정보
     */
    CashDTO getCashByMemberId(Long memberId);

    /**
     * 현금 금액 설정 (없으면 생성, 있으면 업데이트)
     * @param memberId 회원 ID
     * @param cashAmount 현금 금액
     * @return 설정된 현금 정보
     */
    CashDTO setCashAmount(Long memberId, Long cashAmount);
}

package com.banklab.cash.mapper;

import com.banklab.cash.domain.CashVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * 현금 관리 Mapper 인터페이스
 */
@Mapper
public interface CashMapper {

    /**
     * 회원의 현금 정보 조회
     * @param memberId 회원 ID
     * @return 현금 정보 (없으면 null)
     */
    CashVO selectCashByMemberId(@Param("memberId") Long memberId);

    /**
     * 현금 정보 저장 (최초 생성)
     * @param cashVO 현금 정보
     */
    int insertCash(CashVO cashVO);

    /**
     * 현금 금액 업데이트
     * @param memberId 회원 ID
     * @param cashAmount 새로운 현금 금액
     */
    int updateCashAmount(@Param("memberId") Long memberId, @Param("cashAmount") Long cashAmount);

}
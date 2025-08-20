package com.banklab.transaction.summary.service;

import com.banklab.account.domain.AccountVO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;


public interface SummaryBatchService {
    /**
     * 특정 날짜의 거래 내역을 집계하여 일별 요약 테이블에 저장(upsert)합니다.
     * @param targetDate 집계할 날짜
     * @param memberId 특정 사용자 ID (null일 경우 전체 사용자)
     */
    void aggregateDailySummary(LocalDate targetDate, Long memberId);

    /**
     * 사용자가 처음 자산을 연동했을 때, 과거 거래 내역 전체에 대한 일별 요약을 생성합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 정보
     * @param startDate 집계 시작일
     */
    void initDailySummary(Long memberId, AccountVO account, String startDate);

    /**
     * 특정 날짜들의 일별 요약 데이터를 삭제합니다. (카테고리 변경 등으로 재집계가 필요할 때 사용)
     * @param memberId 사용자 ID
     * @param targetDate 삭제할 날짜 리스트
     */
    void deleteDailySummary(Long memberId, List<Date> targetDate);
}

package com.banklab.transaction.summary.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.transaction.summary.dto.DailySummaryDTO;
import com.banklab.transaction.summary.mapper.SummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SummaryBatchServiceImpl implements SummaryBatchService {

    private final SummaryMapper summaryMapper;
    private final MemberMapper memberMapper;

    /**
     * 특정 날짜의 거래 내역을 집계하여 일별 요약 테이블에 저장(upsert)합니다.
     * memberId가 null이면 전체 사용자를 대상으로 실행됩니다.
     * @param targetDate 집계할 날짜
     * @param memberId 특정 사용자 ID (null일 경우 전체 사용자)
     */
    @Override
    @Transactional
    public void aggregateDailySummary(LocalDate targetDate, Long memberId) {
        // 1. 모든 사용자  조회
        List<Long> memberIdList = new ArrayList<>();
        if(memberId==null) {
            memberIdList = memberMapper.findAllMemberIds();
        }else{
            memberIdList.add(memberId);
        }

        for (Long id: memberIdList) {
            // 2. 각 사용자 + 카테고리별 지출/수입 합계 계산
            List<DailySummaryDTO> dailyCategorySummary
                    = summaryMapper.getDailySummary(id, Date.valueOf(targetDate));

            // 3. 받아온 데이터 집계 테이블에 저장
            for (DailySummaryDTO dailySummaryDTO: dailyCategorySummary) {
                summaryMapper.upsertDailySummary(dailySummaryDTO);
                log.info("DailySummary: {}", dailySummaryDTO);
            }
        }
    }

    /**
     * 사용자가 처음 자산을 연동했을 때 호출되어, 지정된 시작일부터 현재까지의 모든 거래 내역에 대한
     * 일별 요약을 생성하고 저장합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 정보
     * @param startDate 집계를 시작할 날짜 (yyyyMMdd 형식)
     */
    @Override
    @Transactional
    public void initDailySummary(Long memberId, AccountVO account, String startDate) {
        // 1. 마지막 집계 일자 구하기
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate lastDay = LocalDate.parse(startDate, formatter);

        // 2. 마지막 일부터 오늘까지 집계테이블 저장
        while (!lastDay.isAfter(today)) {
            aggregateDailySummary(lastDay, memberId);
            lastDay = lastDay.plusDays(1);
        }
    }

    /**
     * 특정 날짜들의 일별 요약 데이터를 삭제합니다.
     * @param memberId 사용자 ID
     * @param targetDate 삭제할 날짜(Date) 리스트
     */
    @Override
    public void deleteDailySummary(Long memberId, List<java.util.Date> targetDate) {
        summaryMapper.deleteDailySummary(memberId, targetDate);
    }

}

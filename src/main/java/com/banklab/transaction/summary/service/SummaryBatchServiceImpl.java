package com.banklab.transaction.summary.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.handler.LoginFailureHandler;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.dto.DailySummaryDTO;
import com.banklab.transaction.summary.mapper.SummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SummaryBatchServiceImpl implements SummaryBatchService {

    private final SummaryMapper summaryMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public void aggregateDailySummary(LocalDate targetDate) {
        // 1. 모든 사용자 조회
        List<Long> memberIdList = memberMapper.findAllMemberIds();
        
        // 2. 모든 사용자의 일일 요약 데이터를 한 번에 수집
        List<DailySummaryDTO> allDailySummaries = new ArrayList<>();
        
        for (Long memberId : memberIdList) {
            // 각 사용자 + 카테고리별 지출/수입 합계 계산 (특정일)
            List<DailySummaryDTO> dailyCategorySummary = summaryMapper.getDailySummary(memberId, Date.valueOf(targetDate));
            allDailySummaries.addAll(dailyCategorySummary);
        }
        
        // 3. 배치로 한 번에 삽입
        if (!allDailySummaries.isEmpty()) {
            summaryMapper.batchUpsertDailySummary(allDailySummaries);
            log.info("Batch inserted {} daily summaries for date: {}", allDailySummaries.size(), targetDate);
        }
    }

    /**
     * 첫 자산 연동 시 호출
     * @param memberId
     * @param account
     */
    @Override
    @Transactional
    public void initDailySummary(Long memberId, AccountVO account) {
        String accountNumber = account.getResAccount();
        
        // 첫 연동은 2년 전 ~ 현재 내역 요청
        LocalDate today = LocalDate.now();
        LocalDate lastDay =today.minusYears(2);

        // 3. 마지막 일부터 오늘까지 집계테이블 저장
        while (!lastDay.isAfter(today)) {
            aggregateDailySummary(lastDay);
            lastDay = lastDay.plusDays(1);
        }
    }
}

package com.banklab.transaction.summary.service;

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
        // 1. 모든 사용자  조회
        List<Long> memberIdList = memberMapper.findAllMemberIds();

        for (Long memberId: memberIdList) {
            // 2. 각 사용자 + 카테고리별 지출/수입 합계 계산
            List<DailySummaryDTO> dailyCategorySummary = summaryMapper.getDailySummary(memberId, Date.valueOf(targetDate));

            // 3. 받아온 데이터 집계 테이블에 저장
            for (DailySummaryDTO dailySummaryDTO: dailyCategorySummary) {
                summaryMapper.upsertDailySummary(dailySummaryDTO);
                log.info("DailySummary: {}", dailySummaryDTO);
            }

        }
    }
}

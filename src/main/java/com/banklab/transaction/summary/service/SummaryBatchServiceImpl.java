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
import java.time.format.DateTimeFormatter;
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
     * 첫 자산 연동 시 호출
     * @param memberId
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
}

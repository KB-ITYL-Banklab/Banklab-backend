package com.banklab.activity.mapper;

import com.banklab.activity.domain.SpendingReportViewLogVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpendingReportViewLogMapper {
    int insert(SpendingReportViewLogVO reportLog);
    List<SpendingReportViewLogVO> findByMemberId(Long memberId);
    int countByMember(Long memberId);
    int countByDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

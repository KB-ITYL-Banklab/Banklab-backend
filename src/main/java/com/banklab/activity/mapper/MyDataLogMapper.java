package com.banklab.activity.mapper;

import com.banklab.activity.domain.MyDataLogVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MyDataLogMapper {
    int insert(MyDataLogVO dataLog);
    List<MyDataLogVO> findByMemberId(Long memberId);
    int countByMember(Long memberId);
    int countByDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    boolean hasRecentFetch(Long memberId);
}

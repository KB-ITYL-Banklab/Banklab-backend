package com.banklab.activity.mapper;

import com.banklab.activity.domain.ContentType;
import com.banklab.activity.domain.ContentViewLogVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentViewLogMapper {
    int insert(ContentViewLogVO contentLog);
    List<ContentViewLogVO> findByMemberId(Long memberId);
    int countByMember(Long memberId);
//    int countByMemberAndType(@Param("memberId") Long memberId, @Param("type") ContentType type);
    int countByDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    boolean hasRecentView(Long memberId);
}

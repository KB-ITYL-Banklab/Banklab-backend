package com.banklab.mission.mapper;

import com.banklab.mission.domain.ExpGrantVO;
import com.banklab.mission.domain.MissionProgressVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MissionProgressMapper {
    MissionProgressVO get(@Param("memberId") Long memberId, @Param("missionId") Integer missionId);
    List<MissionProgressVO> findByMemberId(Long memberId);
    int upsertProgress(MissionProgressVO vo);

    /** 주기당 1회 지급 멱등: UNIQUE(member_id, mission_id, period_start_date) */
    int insertExpGrant(ExpGrantVO vo);
    boolean existsExpGrant(ExpGrantVO vo);
}

package com.banklab.mission.mapper;

import com.banklab.mission.domain.MissionProgressVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MissionProgressMapper {
    MissionProgressVO get(@Param("memberId") Long memberId, @Param("missionId") Integer missionId);
    List<MissionProgressVO> findByMemberId(Long memberId);
    int insert(@Param("memberId") Long memberId, @Param("missionId") Integer missionId);
    int countCompletedMission(@Param("memberId") Long memberId, @Param("missionId") Integer missionId);
    int updateProgress(
            @Param("memberId") Long memberId,
            @Param("missionId") Integer missionId,
            @Param("progressValue") int progressValue);
    int markCompleted(@Param("memberId") Long memberId, @Param("missionId") Integer missionId);
}

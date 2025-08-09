package com.banklab.mission.mapper;

import com.banklab.mission.domain.MissionType;
import com.banklab.mission.domain.MissionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MissionMapper {
    MissionVO findByMissionId(Integer missionId);
    List<MissionVO> findByLevelId(int levelId);
    List<MissionVO> findPreviousSupplementalMissions(int levelId);
    List<MissionVO> findByType(MissionType type);
}

package com.banklab.mission.service;

import com.banklab.mission.domain.MissionVO;
import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.dto.MissionsResponseDTO;

import java.util.List;

public interface MissionService {
    List<MissionDTO> getAvailableMissions(Long memberId);
//    boolean isAlreadyCompleted(Long memberId, Integer missionId);
//    void completeMission(Long memberId, Integer missionId);
    MissionsResponseDTO getAndUpdateMissionProgress(Long memberId);
}

package com.banklab.mission.service;

import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.dto.MissionsResponseDTO;

import java.util.List;

public interface MissionService {
    List<MissionDTO> getAvailableMissions(Long memberId);
    MissionsResponseDTO getAndUpdateMissionProgress(Long memberId);
    void catchUpRewards(Long memberId);
}

package com.banklab.mission.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MissionsResponseDTO {
    private List<MissionStateDTO> required; // 기준/필수
    private List<MissionStateDTO> optional; // 보완
    private List<MissionStateDTO> persistent; // 지속성
}

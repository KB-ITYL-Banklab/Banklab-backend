package com.banklab.mission.dto;

import com.banklab.mission.domain.MissionCycle;
import com.banklab.mission.domain.MissionType;
import com.banklab.mission.domain.MissionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDTO {
    private MissionType type;
    private String title;
    private String description;
    private int rewardExp;
    private int targetValue;
    private MissionCycle missionCycle;

    public static MissionDTO of(MissionVO m) {
        return MissionDTO.builder()
                .type(m.getType())
                .title(m.getTitle())
                .description(m.getDescription())
                .rewardExp(m.getRewardExp())
                .targetValue(m.getTargetValue())
                .missionCycle(m.getMissionCycle())
                .build();
    }
}

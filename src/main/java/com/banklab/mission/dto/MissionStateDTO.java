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
public class MissionStateDTO {
    private MissionType type;
    private String title;
    private String description;
    private int rewardExp;
    private int targetValue;
    private MissionCycle missionCycle;
    private int progressValue;
    private boolean completed;

    public static MissionStateDTO from(MissionVO mission,int value, boolean completed) {
        return MissionStateDTO.builder()
                .type(mission.getType())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .rewardExp(mission.getRewardExp())
                .targetValue(mission.getTargetValue())
                .missionCycle(mission.getMissionCycle())
                .progressValue(value)
                .completed(completed)
                .build();
    }
}

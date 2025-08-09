package com.banklab.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionVO {
    private Integer missionId;
    private Integer levelId;
    private MissionType type;
    private String title;
    private String description;
    private int rewardExp;
    private ConditionKey conditionKey;
    private ConditionType conditionType;
    private int targetValue;
    private MissionCycle missionCycle;
}

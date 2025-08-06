package com.banklab.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionCycle {
    NONE("없음"),
    DAILY("일일 반복"),
    WEEKLY("주간 반복"),
    MONTHLY("월간 반복");

    private final String label;
}

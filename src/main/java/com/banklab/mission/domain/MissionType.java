package com.banklab.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionType {
    CRITERIA("기준"),
    REQUIRED("필수"),
    OPTIONAL("보완"),
    PERSISTENT("지속성");

    private final String label;
}

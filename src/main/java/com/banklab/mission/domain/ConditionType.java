package com.banklab.mission.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionType {
    COUNT("횟수 조건"),
    RATIO("비율 조건"),
    BOOLEAN("충족 여부 조건");

    private final String label;
}

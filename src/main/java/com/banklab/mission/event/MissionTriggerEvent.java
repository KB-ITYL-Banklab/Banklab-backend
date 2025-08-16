package com.banklab.mission.event;

import com.banklab.mission.domain.ConditionKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionTriggerEvent {
    private Long memberId;
    private ConditionKey conditionKey;
    private LocalDate activityDate;
}
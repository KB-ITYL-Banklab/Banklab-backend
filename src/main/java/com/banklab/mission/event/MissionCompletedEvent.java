package com.banklab.mission.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MissionCompletedEvent {
    private final Long memberId;
    private final int missionId;
    private final int rewardExp;
}

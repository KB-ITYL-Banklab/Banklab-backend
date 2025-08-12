package com.banklab.mission.service;

import com.banklab.mission.domain.ConditionKey;

public interface MissionProgressService {
    void onEvent(Long memberId, ConditionKey conditionKey);
    void onCriteriaChanged(Long memberId);
}

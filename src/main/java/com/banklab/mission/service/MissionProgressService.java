package com.banklab.mission.service;

import com.banklab.mission.domain.ConditionKey;

public interface MissionProgressService {
    /**
     * 이벤트 발생 시 관련 미션 평가 진행
     * @param memberId
     * @param conditionKey
     */
    void onEvent(Long memberId, ConditionKey conditionKey);

    /**
     * 자산 변동 시 관련 미션 평가 진행
     * @param memberId
     */
    void onCriteriaChanged(Long memberId);
}

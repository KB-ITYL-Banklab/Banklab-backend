package com.banklab.mission.evaluator;

import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.domain.MissionVO;

import java.util.Set;

public interface MissionEvaluator {
    // 이 평가기가 처리할 ConditionKey 목록
    Set<ConditionKey> supportedKeys();
    int evaluate(Long memberId, MissionVO mission);
}

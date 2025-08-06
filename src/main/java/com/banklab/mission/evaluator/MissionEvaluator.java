package com.banklab.mission.evaluator;

import com.banklab.mission.domain.MissionVO;

public interface MissionEvaluator {
    boolean evaluate(Long memberId, MissionVO mission);
}

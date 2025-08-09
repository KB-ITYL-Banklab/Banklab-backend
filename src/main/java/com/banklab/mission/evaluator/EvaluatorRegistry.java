package com.banklab.mission.evaluator;

import com.banklab.mission.domain.ConditionKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EvaluatorRegistry {
    private final Map<ConditionKey, MissionEvaluator> evaluatorMap = new ConcurrentHashMap<>();

    public EvaluatorRegistry(List<MissionEvaluator> evaluators) {
        for (MissionEvaluator e : evaluators) {
            for (ConditionKey key : e.supportedKeys()) {
                if (evaluatorMap.putIfAbsent(key, e) != null) {
                    throw new IllegalStateException("Duplicate evaluator for key: " + key);
                }
            }
        }
    }

    // MissionEvaluator 구현체가 Bean 등록되면서 자동 주입되도록 구성
    public void register(ConditionKey key, MissionEvaluator evaluator) {
        evaluatorMap.put(key, evaluator);
    }

    public MissionEvaluator getEvaluator(ConditionKey conditionKey) {
        MissionEvaluator evaluator = evaluatorMap.get(conditionKey);
        if (evaluator == null) {
            throw new IllegalArgumentException("Evaluator not found for conditionKey: " + conditionKey);
        }
        return evaluator;
    }
}

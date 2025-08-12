package com.banklab.mission.service;

import com.banklab.character.service.CharacterService;
import com.banklab.mission.domain.*;
import com.banklab.mission.evaluator.EvaluatorRegistry;
import com.banklab.mission.evaluator.MissionEvaluator;
import com.banklab.mission.event.MissionCompletedEvent;
import com.banklab.mission.mapper.MissionMapper;
import com.banklab.mission.mapper.MissionProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionProgressServiceImpl implements MissionProgressService {

    private final MissionMapper missionMapper;
    private final MissionProgressMapper missionProgressMapper;
    private final CharacterService characterService;
    private final EvaluatorRegistry evaluatorRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void onEvent(Long memberId, ConditionKey conditionKey) {
        int currentLevel = characterService.lockAndGetLevel(memberId); // FOR UPDATE
        List<MissionVO> missions = missionMapper.findByLevelAndKey(currentLevel + 1, conditionKey.name());
        if (missions.isEmpty()) return;

        for (MissionVO m : missions) {
            // 이미 완료(= 이미 지급된 주기)면 스킵 — exp_grant_log로 판정
            ExpGrantVO grantVO = ExpGrantVO.from(memberId, m);
            if (missionProgressMapper.existsExpGrant(grantVO)) {
                continue;
            }

            boolean completed = isCompleted(memberId, m);

            if (!completed) continue;

            // 보상 직전 레벨 재검증(경합 대비)
            int levelNow = characterService.lockAndGetLevel(memberId);
            if (levelNow != currentLevel) continue;

            boolean isPersistent = MissionType.PERSISTENT.equals(m.getType());
            if (!isPersistent) {
                int inserted = missionProgressMapper.insertExpGrant(grantVO);
                if (inserted > 0) {
                    eventPublisher.publishEvent(
                            new MissionCompletedEvent(memberId, m.getMissionId(), m.getRewardExp())
                    );
                }
            }
        }
    }

    @Transactional
    @Override
    public void onCriteriaChanged(Long memberId) {
        int currentLevel = characterService.lockAndGetLevel(memberId);
        // 자산기반 미션들만(현재 레벨) 뽑기
        List<MissionVO> missions = missionMapper.findByType(MissionType.CRITERIA);
        if (missions.isEmpty()) return;

        for (MissionVO m : missions) {
            // 완료 여부(log) 선검사
            ExpGrantVO grantVO = ExpGrantVO.from(memberId, m);
            if ((m.getLevelId() != (currentLevel + 1)) || missionProgressMapper.existsExpGrant(grantVO)) continue;

            boolean completed = isCompleted(memberId, m);

            if (!completed) continue;

            // 보상 직전 레벨 재검증
            int levelNow = characterService.lockAndGetLevel(memberId);
            if (levelNow != m.getLevelId()) continue;

            // 멱등 지급
            int inserted = missionProgressMapper.insertExpGrant(grantVO);
            if (inserted > 0) {
                eventPublisher.publishEvent(
                        new MissionCompletedEvent(memberId, m.getMissionId(), m.getRewardExp())
                );
            }
        }
    }

    private boolean isCompleted(Long memberId, MissionVO mission) {
        MissionEvaluator evaluator = evaluatorRegistry.getEvaluator(mission.getConditionKey());
        int progressValue = evaluator.evaluate(memberId, mission);
        boolean completed = progressValue >= mission.getTargetValue();

        missionProgressMapper.upsertProgress(MissionProgressVO.from(memberId, mission, progressValue));
        return completed;
    }
}

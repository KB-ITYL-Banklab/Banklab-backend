package com.banklab.mission.service;

import com.banklab.character.service.CharacterService;
import com.banklab.mission.domain.ExpGrantVO;
import com.banklab.mission.domain.MissionType;
import com.banklab.mission.dto.MissionStateDTO;
import com.banklab.mission.dto.MissionsResponseDTO;
import com.banklab.mission.domain.MissionVO;
import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.evaluator.EvaluatorRegistry;
import com.banklab.mission.evaluator.MissionEvaluator;
import com.banklab.mission.mapper.MissionMapper;
import com.banklab.mission.mapper.MissionProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionMapper missionMapper;
    private final MissionProgressMapper missionProgressMapper;
    private final CharacterService characterService;
    private final EvaluatorRegistry evaluatorRegistry;


    @Override
    public List<MissionDTO> getAvailableMissions(Long memberId) {
        int currentLevel = characterService.getCharacter(memberId).getLevel();
        return getCurrentLevelMissions(currentLevel).stream()
            .map(MissionDTO::of)
            .collect(Collectors.toList());
    }

    private List<MissionVO> getCurrentLevelMissions(int currentLevel) {
        // 다음 레벨 미션들 조회
        int nextLevel = currentLevel + 1;
        List<MissionVO> nextLevelMissions = missionMapper.findByLevelId(nextLevel);

        // 이전 레벨의 보완/지속성 미션들 조회
        List<MissionVO> previousOptionalMissions = missionMapper.findPreviousSupplementalMissions(nextLevel);

        // 합쳐서 반환
        List<MissionVO> all = new ArrayList<>();
        all.addAll(nextLevelMissions);
        all.addAll(previousOptionalMissions);

        return all;
    }

    @Transactional
    @Override
    public void catchUpRewards(Long memberId) {
        Integer level = characterService.lockAndGetLevel(memberId); // FOR UPDATE
        if (level == null) throw new IllegalStateException("Level is null");

        List<MissionVO> missions = getCurrentLevelMissions(level);
        if (missions.isEmpty()) return;

        for (MissionVO m : missions) {
            // 이미 지급된 주기면 스킵
            boolean isPersistent = MissionType.PERSISTENT.equals(m.getType());
            ExpGrantVO grant = ExpGrantVO.from(memberId, m); // periodStart = Periods.periodStart(cycle)
            if (isPersistent || missionProgressMapper.existsExpGrant(grant)) continue;

            // 평가 (activity_daily_agg / 결과테이블 기반 Evaluator들이 알아서 계산)
            MissionEvaluator ev = evaluatorRegistry.getEvaluator(m.getConditionKey());
            if (ev == null) continue;

            int result = ev.evaluate(memberId, m); // progress or EvaluationResult
            boolean completed = (result >= m.getTargetValue());
            if (!completed) continue;

            // 멱등 지급 + EXP 반영
            if (missionProgressMapper.insertExpGrant(grant) > 0) {
                characterService.addExpAndLevelUp(memberId, m.getRewardExp());
            }
        }
    }

    @Override
    @Transactional
    public MissionsResponseDTO getAndUpdateMissionProgress(Long memberId) {
        int currentLevel = characterService.getCharacter(memberId).getLevel();
        List<MissionVO> missions = getCurrentLevelMissions(currentLevel);

        List<MissionStateDTO> required = new ArrayList<>();
        List<MissionStateDTO> optional = new ArrayList<>();
        List<MissionStateDTO> persistent = new ArrayList<>();

        for (MissionVO mission : missions) {
            // 완료 여부: exp_grant_log 존재로 판단 (주기당 1회 지급 멱등)
            boolean completed = missionProgressMapper.existsExpGrant(ExpGrantVO.from(memberId, mission));

            int progressValue = 0;
            if (!completed) {
                // 진행도 계산은 Evaluator에게 (recent_days > mission_cycle > 누적)
                MissionEvaluator evaluator = evaluatorRegistry.getEvaluator(mission.getConditionKey());
                if (evaluator != null) {
                    progressValue = evaluator.evaluate(memberId, mission);
                }
                completed = progressValue >= mission.getTargetValue();
            }

            // DTO 구성 (완료 여부는 지급 로그 기준)
            MissionStateDTO dto = MissionStateDTO.from(mission, completed ? mission.getTargetValue() : progressValue, completed);

            switch (mission.getType()) {
                case REQUIRED, CRITERIA -> required.add(dto);
                case OPTIONAL -> optional.add(dto);
                case PERSISTENT -> persistent.add(dto);
            }
        }

        return MissionsResponseDTO.builder()
                .required(required)
                .optional(optional)
                .persistent(persistent)
                .build();
    }
}

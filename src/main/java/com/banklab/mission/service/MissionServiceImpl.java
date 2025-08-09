package com.banklab.mission.service;

import com.banklab.character.domain.MemberCharacterVO;
import com.banklab.character.dto.CharacterDTO;
import com.banklab.character.service.CharacterService;
import com.banklab.mission.domain.MissionType;
import com.banklab.mission.dto.MissionStateDTO;
import com.banklab.mission.dto.MissionsResponseDTO;
import com.banklab.mission.event.MissionCompletedEvent;
import com.banklab.mission.domain.MissionProgressVO;
import com.banklab.mission.domain.MissionVO;
import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.evaluator.EvaluatorRegistry;
import com.banklab.mission.evaluator.MissionEvaluator;
import com.banklab.mission.mapper.MissionMapper;
import com.banklab.mission.mapper.MissionProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionMapper missionMapper;
    private final MissionProgressMapper missionProgressMapper;
    private final CharacterService characterService;
    private final EvaluatorRegistry evaluatorRegistry;
    private final ApplicationEventPublisher eventPublisher;


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
        List<MissionVO> previousOptionalMissions = missionMapper.findPreviousSupplementalMissions(currentLevel);

        // 합쳐서 반환
        List<MissionVO> all = new ArrayList<>();
        all.addAll(nextLevelMissions);
        all.addAll(previousOptionalMissions);

        return all;
    }

    @Override
    @Transactional
    public MissionsResponseDTO getAndUpdateMissionProgress(Long memberId) {
        int currentLevel = characterService.getCharacter(memberId).getLevel();
        List<MissionVO> missions = getCurrentLevelMissions(currentLevel);

        List<MissionStateDTO> required = new ArrayList<>();
        List<MissionStateDTO> optional = new ArrayList<>();
        List<MissionStateDTO> persistent = new ArrayList<>();

        // 기존 진행도
        // 2) 기존 진행도 맵 (missionId -> progress)
        Map<Integer, MissionProgressVO> progressMap = missionProgressMapper.findByMemberId(memberId)
                .stream()
                .collect(Collectors.toMap(MissionProgressVO::getMissionId, p -> p));

        // 없으면 삽입
        for (MissionVO mission : missions) {
            if (!progressMap.containsKey(mission.getMissionId())) {
                missionProgressMapper.insert(memberId, mission.getMissionId());
                progressMap.put(mission.getMissionId(), missionProgressMapper.get(memberId, mission.getMissionId()));
            }
        }

        // 평가 및 갱신
        for (MissionVO mission : missions) {
            MissionProgressVO prev = progressMap.get(mission.getMissionId());
            // 진행도 값은 항상 최신화 (단, 완료 플래그는 다운그레이드 금지)
            boolean alreadyCompleted = prev.isCompleted();

            int progressValue = prev.getProgressValue();
            boolean completed = false;
            if(!alreadyCompleted) {
                MissionEvaluator evaluator = evaluatorRegistry.getEvaluator(mission.getConditionKey());
                progressValue = evaluator.evaluate(memberId, mission);
                completed = progressValue >= mission.getTargetValue();

                if (prev.getProgressValue() != progressValue)
                    missionProgressMapper.updateProgress(memberId, mission.getMissionId(), progressValue);

                // 지속성(PERSISTENT) 미션은 “유지 충족” 상태만 갱신하고 보상은 없음(설계에 따라)
                boolean isPersistent = mission.getType().equals(MissionType.PERSISTENT);

                // 지속성이 아니고, 목표 달성했을 때만 '전이 완료' 시도
                if (!isPersistent && completed) {
                    int changed = missionProgressMapper.markCompleted(memberId, mission.getMissionId());
                    if (changed == 1) { // 이번에 처음 완료됨
                        eventPublisher.publishEvent(
                                new MissionCompletedEvent(memberId, mission.getMissionId(), mission.getRewardExp())
                        );
                    }
                }
            }

            MissionStateDTO dto = MissionStateDTO.from(mission, progressValue, alreadyCompleted || completed);
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

//    @Override
//    public boolean isAlreadyCompleted(Long memberId, Integer missionId) {
//        int count = missionProgressMapper.countCompletedMission(memberId, missionId);
//        return count > 0;
//    }
//
//    @Override
//    @Transactional
//    public void completeMission(Long memberId, Integer missionId) {
//        MissionVO mission = missionMapper.findByMissionId(missionId);
//        if (isAlreadyCompleted(memberId, missionId)) return;
//        if (!missionEvaluator.evaluate(memberId, mission)) return;
//
//        // 완료 처리
//        missionMapper.markCompleted(memberId, missionId);
//
//        // 이벤트 발행 (커밋 후 처리 예정)
//        eventPublisher.publishEvent(
//                new MissionCompletedEvent(memberId, missionId, mission.getRewardExp())
//        );
//    }

}

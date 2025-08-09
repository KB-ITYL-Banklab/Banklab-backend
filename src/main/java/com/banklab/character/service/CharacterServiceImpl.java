package com.banklab.character.service;

import com.banklab.character.domain.CharacterLevelVO;
import com.banklab.character.domain.CharacterVO;
import com.banklab.character.domain.MemberCharacterVO;
import com.banklab.character.dto.CharacterDTO;
import com.banklab.character.mapper.CharacterMapper;
import com.banklab.mission.domain.MissionType;
import com.banklab.mission.domain.MissionVO;
import com.banklab.mission.evaluator.CriteriaMissionEvaluator;
import com.banklab.mission.mapper.MissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharacterServiceImpl implements CharacterService {
    private final CharacterMapper characterMapper;
    private final MissionMapper missionMapper;
    private final CriteriaMissionEvaluator criteriaMissionEvaluator;

    @Override
    public CharacterDTO getCharacter(Long memberId) {
        MemberCharacterVO character = Optional.ofNullable(characterMapper.getMemberCharacter(memberId))
                .orElseThrow(NoSuchElementException::new);
        int nextExp = 2000;
        int nextLevel = character.getCurrentLevel().getLevelId() + 1;
        if (nextLevel < 7) {
            nextExp = characterMapper.getLevelInfo(nextLevel).getRequiredExp();
        }
        return CharacterDTO.of(character, nextExp);
    }

    @Transactional
    @Override
    public CharacterDTO createCharacter(Long memberId) {
        if (characterMapper.getMemberCharacter(memberId) != null) {
            throw new IllegalStateException("이미 생성된 캐릭터가 있습니다.");
        }

        List<MissionVO> criteriaMissions = missionMapper.findByType(MissionType.CRITERIA);
        int startingLevel = determineStartingLevel(memberId, criteriaMissions);
        int startExp = characterMapper.getLevelInfo(startingLevel).getRequiredExp();

        CharacterVO newCharacter = CharacterVO.builder()
                .memberId(memberId)
                .currentLevel(startingLevel)
                .exp(startExp)
                .build();

        characterMapper.insertCharacter(newCharacter);
        return getCharacter(memberId);
    }

    private int determineStartingLevel(Long memberId, List<MissionVO> criteriaMissions) {
        Map<Integer, List<MissionVO>> missionsByLevel = criteriaMissions.stream()
                .collect(Collectors.groupingBy(MissionVO::getLevelId));

        for (int level = 4; level >= 1; level--) {
            List<MissionVO> levelCriteria = missionsByLevel.getOrDefault(level, List.of());
            boolean passed = levelCriteria.stream()
                    .allMatch(mission -> criteriaMissionEvaluator.evaluate(memberId, mission) >= mission.getTargetValue());
            if (passed) return level;
        }

        return 1;
    }


//    @Override
//    public void syncLevelAndExp(Long memberId) {
//        MemberCharacterVO character = characterMapper.getMemberCharacter(memberId);
//
//        int gainedExp = missionService.calculateExp(memberId);
//        character.addExp(gainedExp);
//
//        // 레벨업 가능한 레벨까지 체크
//        int nextLevel = character.getCurrentLevel().getLevelId() + 1;
//        CharacterLevelVO next = characterMapper.getLevelInfo(nextLevel);
//        while (character.canLevelUp(next)) {
//            character.levelUp(next);
//            next = characterMapper.getLevelInfo(next.getLevelId() + 1);
//        }
//
//        // 변경된 상태 저장
//        characterMapper.updateCharacter(memberId, character.getCurrentLevel().getLevelId(), character.getExp());
//    }

    @Override
    @Transactional
    public boolean addExpAndLevelUp(Long memberId, int gainedExp) {
        if (gainedExp <= 0) return false;

        // 현재 캐릭터 상태 조회 (경합 방지하려면 for update 사용 권장)
        MemberCharacterVO character = characterMapper.getMemberCharacterForUpdate(memberId);
        if (character == null) throw new NoSuchElementException("Character not found: " + memberId);

        // 2) 경험치 추가
        character.addExp(gainedExp);

        // 연속 레벨업 판단
        boolean leveledUp = false;
        int nextLevel = character.getCurrentLevel().getLevelId() + 1;
        CharacterLevelVO next = characterMapper.getLevelInfo(nextLevel);
        while (character.canLevelUp(next)) {
            character.levelUp(next);
            leveledUp = true;
            next = characterMapper.getLevelInfo(next.getLevelId() + 1);
        }

        // 변경사항 DB 반영 (exp, level 동시 업데이트)
        characterMapper.updateCharacter(
                memberId,
                character.getCurrentLevel().getLevelId(),
                character.getExp()
        );

        return leveledUp;
    }
}

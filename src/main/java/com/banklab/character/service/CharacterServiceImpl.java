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
        return CharacterDTO.of(character);
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
                    .allMatch(mission -> criteriaMissionEvaluator.evaluate(memberId, mission));
            if (passed) return level;
        }

        return 1;
    }

}

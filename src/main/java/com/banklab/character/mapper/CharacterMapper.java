package com.banklab.character.mapper;

import com.banklab.character.domain.CharacterLevelVO;
import com.banklab.character.domain.CharacterVO;
import com.banklab.character.domain.MemberCharacterVO;

public interface CharacterMapper {
    MemberCharacterVO getMemberCharacter(Long memberId);
    CharacterLevelVO getLevelInfo(int levelId);
    int insertCharacter(CharacterVO character);
}

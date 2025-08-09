package com.banklab.character.mapper;

import com.banklab.character.domain.CharacterLevelVO;
import com.banklab.character.domain.CharacterVO;
import com.banklab.character.domain.MemberCharacterVO;
import org.apache.ibatis.annotations.Param;

public interface CharacterMapper {
    MemberCharacterVO getMemberCharacter(Long memberId);
    MemberCharacterVO getMemberCharacterForUpdate(Long memberId);
    CharacterLevelVO getLevelInfo(int levelId);
    int insertCharacter(CharacterVO character);
    int updateCharacter(@Param("memberId") Long memberId, @Param("levelId") int levelId, @Param("exp") int exp);
}

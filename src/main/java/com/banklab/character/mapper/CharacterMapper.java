package com.banklab.character.mapper;

import com.banklab.character.domain.MemberCharacterVO;

public interface CharacterMapper {
    MemberCharacterVO get(Long memberId);
}

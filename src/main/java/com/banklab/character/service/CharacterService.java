package com.banklab.character.service;

import com.banklab.character.dto.CharacterDTO;

public interface CharacterService {
    CharacterDTO getCharacter(Long memberId);
}

package com.banklab.character.service;

import com.banklab.character.domain.MemberCharacterVO;
import com.banklab.character.dto.CharacterDTO;
import com.banklab.character.mapper.CharacterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterServiceImpl implements CharacterService {
    private final CharacterMapper mapper;

    @Override
    public CharacterDTO getCharacter(Long memberId) {
        MemberCharacterVO character = Optional.ofNullable(mapper.get(memberId))
                .orElseThrow(NoSuchElementException::new);
        return CharacterDTO.of(character);
    }
}

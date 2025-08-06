package com.banklab.character.dto;

import com.banklab.character.domain.MemberCharacterVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterDTO {
    private int level;
    private int exp;
    private String name;
    private String description;
    private int requiredExp;

    public static CharacterDTO of(MemberCharacterVO vo) {
        return CharacterDTO.builder()
                .level(vo.getCurrentLevel().getLevelId())
                .exp(vo.getExp())
                .name(vo.getCurrentLevel().getName())
                .description(vo.getCurrentLevel().getDescription())
                .requiredExp(vo.getCurrentLevel().getRequiredExp())
                .build();
    }
}

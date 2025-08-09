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
    private int exp;            // 현재 멤버 경험치
    private String name;
    private String description;
    private int requiredExp;    // 현재 레벨 요구 경험치 (진행률 표시를 위해 필요)
    private int nextExp;       // 다음 레벨 요구 경험치
    private String imageUrl;

    public static CharacterDTO of(MemberCharacterVO vo, int nextExp) {
        return CharacterDTO.builder()
                .level(vo.getCurrentLevel().getLevelId())
                .exp(vo.getExp())
                .name(vo.getCurrentLevel().getName())
                .description(vo.getCurrentLevel().getDescription())
                .requiredExp(vo.getCurrentLevel().getRequiredExp())
                .nextExp(nextExp)
                .imageUrl(vo.getCurrentLevel().getImageUrl())
                .build();
    }
}

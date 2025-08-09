package com.banklab.character.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterLevelVO {
    private int levelId;
    private String name;
    private String description;
    private int requiredExp;
    private String imageUrl;
}

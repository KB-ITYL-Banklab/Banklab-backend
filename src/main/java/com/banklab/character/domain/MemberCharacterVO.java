package com.banklab.character.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCharacterVO {
    private Long memberId;
    private int exp;
    private Date createdAt;
    private Date updatedAt;
    private CharacterLevelVO currentLevel;

    public void addExp(int gainedExp) {
        this.exp += gainedExp;
    }

    public boolean canLevelUp(CharacterLevelVO nextLevel) {
        return nextLevel != null && this.exp >= nextLevel.getRequiredExp();
    }

    public void levelUp(CharacterLevelVO nextLevel) {
        this.currentLevel = nextLevel;
    }
}

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
public class CharacterVO {
    private Long memberId;
    private int currentLevel;
    private int exp;
    private Date createdAt;
    private Date updatedAt;
}

package com.banklab.character.service;

import com.banklab.character.dto.CharacterDTO;

public interface CharacterService {
    /**
     * 멤버 캐릭터 조회
     * @param memberId
     * @return 캐릭터 정보 DTO
     */
    CharacterDTO getCharacter(Long memberId);

    /**
     * 멤버 캐릭터 생성
     * @param memberId
     * @return 생성된 캐릭터 정보 DTO
     */
    CharacterDTO createCharacter(Long memberId);

    /**
     * lock 멤버 캐릭터 레벨 조회
     * @param memberId
     * @return
     */
    Integer lockAndGetLevel(Long memberId);

    /**
     * 경험치 계산 및 레벨업 확인
     * @param memberId
     * @param gainedExp
     * @return
     */
    boolean addExpAndLevelUp(Long memberId, int gainedExp);
}

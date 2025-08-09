package com.banklab.character.controller;

import com.banklab.character.dto.CharacterDTO;
import com.banklab.character.service.CharacterService;
import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.dto.MissionsResponseDTO;
import com.banklab.mission.service.MissionService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/character")
public class CharacterController {
    private final CharacterService service;
    private final MissionService missionService;
    private final LoginUserProvider loginUserProvider;

    @GetMapping("")
    @ApiOperation(value = "멤버 캐릭터 조회")
    public ResponseEntity<CharacterDTO> getMemberCharacter() {
        Long memberId = loginUserProvider.getLoginMemberId();
        return ResponseEntity.ok(service.getCharacter(memberId));
    }

    @PostMapping("")
    @ApiOperation(value = "캐릭터 생성")
    public ResponseEntity<CharacterDTO> createCharacter() {
        Long memberId = loginUserProvider.getLoginMemberId();
        return ResponseEntity.ok(service.createCharacter(memberId));
    }

    @GetMapping("/missions")
    @ApiOperation(value = "캐릭터 페이지 진입 시: 레벨 동기화 + 미션 평가 + 미션 목록 조회")
    public ResponseEntity<?> syncCharacterAndGetMissions() {
        Long memberId = loginUserProvider.getLoginMemberId();

        // 1. 캐릭터 경험치/레벨 동기화
//        service.syncLevelAndExp(memberId);

        // 2. 현재 레벨 미션 평가 및 진행도 갱신 → 미션 목록 반환
        MissionsResponseDTO missions = missionService.getAndUpdateMissionProgress(memberId);

        return ResponseEntity.ok(missions);
    }


    @PostMapping("/sync")
    public ResponseEntity<?> syncLevelAndExp() {
        Long memberId = loginUserProvider.getLoginMemberId();
        return null;
    }
}

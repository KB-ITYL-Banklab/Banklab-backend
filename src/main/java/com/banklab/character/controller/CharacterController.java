package com.banklab.character.controller;

import com.banklab.character.dto.CharacterDTO;
import com.banklab.character.service.CharacterService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/character")
public class CharacterController {
    private final CharacterService service;
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


    @PostMapping("/sync")
    public ResponseEntity<?> syncLevelAndExp() {
        Long memberId = loginUserProvider.getLoginMemberId();
        return null;
    }
}

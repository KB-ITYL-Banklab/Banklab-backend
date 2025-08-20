package com.banklab.mission.controller;

import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.service.MissionService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mission")
public class MissionController {
    private final MissionService missionService;
    private final LoginUserProvider loginUserProvider;

    @GetMapping("")
    @ApiOperation(value = "현재 레벨 미션 전체 조회")
    public ResponseEntity<?> getCurrentLevelMissions() {
        Long memberId = loginUserProvider.getLoginMemberId();
        List<MissionDTO> missions = missionService.getAvailableMissions(memberId);
        return ResponseEntity.ok(missions);
    }
}

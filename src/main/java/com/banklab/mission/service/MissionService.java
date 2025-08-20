package com.banklab.mission.service;

import com.banklab.mission.dto.MissionDTO;
import com.banklab.mission.dto.MissionsResponseDTO;

import java.util.List;

public interface MissionService {
    /**
     * 회원 캐릭터의 현재 레벨에서 주어진 전체 미션 목록 조회
     * @param memberId
     * @return 전체 미션 목록
     */
    List<MissionDTO> getAvailableMissions(Long memberId);

    /**
     * 미션 평가 후 미션 진행도 갱신/조회
     * @param memberId
     * @return 전체 미션 목록
     */
    MissionsResponseDTO getAndUpdateMissionProgress(Long memberId);

    /**
     * 캐릭터 생성 전 이미 완료된 미션 처리
     * @param memberId
     */
    void catchUpRewards(Long memberId);
}

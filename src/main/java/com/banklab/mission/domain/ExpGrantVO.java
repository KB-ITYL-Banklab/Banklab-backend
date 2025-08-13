package com.banklab.mission.domain;

import com.banklab.common.util.Periods;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpGrantVO {
    private Long memberId;
    private Integer missionId;
    private String missionCycle;
    private LocalDate periodStartDate; // KST 기준 주기 시작일
    private Integer grantedExp;

    public static ExpGrantVO from(Long memberId, MissionVO mission) {
        return ExpGrantVO.builder()
                .memberId(memberId)
                .missionId(mission.getMissionId())
                .missionCycle(mission.getMissionCycle().name())
                .periodStartDate(Periods.periodStart(mission.getMissionCycle()))
                .grantedExp(mission.getRewardExp())
                .build();
    }
}

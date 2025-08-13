package com.banklab.mission.domain;

import com.banklab.common.util.Periods;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionProgressVO {
    private Long id;
    private Long memberId;
    private int missionId;
    private int progressValue;
    private LocalDate periodStartDate;
    private Date createdAt;
    private Date updatedAt;

    public static MissionProgressVO from(Long memberId, MissionVO mission, int progressValue) {
        return builder()
                .memberId(memberId)
                .missionId(mission.getMissionId())
                .periodStartDate(Periods.periodStart(mission.getMissionCycle()))
                .progressValue(progressValue)
                .build();
    }
}

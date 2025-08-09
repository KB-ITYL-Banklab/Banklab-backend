package com.banklab.mission.domain;

import lombok.*;

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
    private boolean completed;
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;
}

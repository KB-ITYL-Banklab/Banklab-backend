package com.banklab.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionProgressDTO {
    private Long memberId;
    private Integer missionId;
    private int progressValue;
    private boolean completed;
    private Date updatedAt;
}

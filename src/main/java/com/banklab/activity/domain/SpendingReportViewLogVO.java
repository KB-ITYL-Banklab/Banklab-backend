package com.banklab.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingReportViewLogVO {
    private Long id;
    private Long memberId;
    private String reportMonth;
    private Date viewDate;
}

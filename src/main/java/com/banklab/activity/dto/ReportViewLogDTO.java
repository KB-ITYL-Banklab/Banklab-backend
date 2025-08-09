package com.banklab.activity.dto;

import com.banklab.activity.domain.SpendingReportViewLogVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportViewLogDTO {
    private String reportMonth;

    public SpendingReportViewLogVO toVO(Long memberId) {
        return SpendingReportViewLogVO.builder()
                .memberId(memberId)
                .reportMonth(reportMonth)
                .build();
    }
}

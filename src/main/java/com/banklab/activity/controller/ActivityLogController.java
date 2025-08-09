package com.banklab.activity.controller;

import com.banklab.activity.dto.ContentViewLogDTO;
import com.banklab.activity.dto.MyDataFetchLogDTO;
import com.banklab.activity.dto.ReportViewLogDTO;
import com.banklab.activity.service.ActivityService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activity-logs")
public class ActivityLogController {
    private final ActivityService activityService;
    private final LoginUserProvider loginUserProvider;

    @PostMapping("/content-view")
    @ApiOperation(value = "금융 컨텐츠 열람 로그")
    public void logContentView(@RequestBody ContentViewLogDTO request) {
        Long memberId = loginUserProvider.getLoginMemberId();
    }

    @PostMapping("/compare")
    @ApiOperation(value = "상품 비교 기능 사용 로그")
    public void logCompareUsage() {
        Long memberId = loginUserProvider.getLoginMemberId();
    }

    @PostMapping("/report-view")
    @ApiOperation(value = "소비 분석 리포트 열람 로그")
    public void logSpendingReportView(@RequestBody ReportViewLogDTO request) {
        Long memberId = loginUserProvider.getLoginMemberId();
        activityService.saveReportViewLog(memberId, request);
    }

    @PostMapping("/mydata-fetch")
    @ApiOperation(value = "마이데이터 조회 로그")
    public void logMyDataFetch(@RequestBody MyDataFetchLogDTO request) {
        Long memberId = loginUserProvider.getLoginMemberId();
    }
}

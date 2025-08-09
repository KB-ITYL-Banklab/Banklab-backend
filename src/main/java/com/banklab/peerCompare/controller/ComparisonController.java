package com.banklab.peerCompare.controller;

import com.banklab.common.response.HTTPResponse;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import com.banklab.peerCompare.dto.PeerComparisonResponseDTO;
import com.banklab.peerCompare.service.ComparisonService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/peer")
@RequiredArgsConstructor
@Log4j2
@ApiOperation(value = "또래 대비 소비 패턴 비교", notes = "현재 로그인한 사용자의 소비 패턴을 같은 연령대 또래와 비교 분석")
public class ComparisonController {
    private final LoginUserProvider loginUserProvider;
    private final ComparisonService comparisonService;

    private Map<String, Object> extractAuthInfo(){
        Long memberId = loginUserProvider.getLoginMemberId();
        String email = loginUserProvider.getLoginEmail();

        if(memberId==null || email == null)
            throw new SecurityException("인증이 필요합니다. 로그인 후 다시 시도하세요");

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("memberId",memberId);
        authInfo.put("email",email);

        return authInfo;
    }

    @GetMapping("/")
    @ApiOperation(value = "또래 대비 소비 패턴 비교", notes = "현재 로그인한 사용자의 소비 패턴을 같은 연령대 또래와 비교 분석.")
    public ResponseEntity<Map<String, Object>> getPeerComparison(
            @ApiParam(value = "비교 분석 시작일", example = "2024-07-01")
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,

            @ApiParam(value = "비교 분석 종료일", example = "2024-07-31")
            @RequestParam(value = "end",required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ){
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            PeerComparisonResponseDTO peerComaprison = comparisonService.getPeerCategoryCompare(memberId, email, startDate, endDate);
            response.put("peer", peerComaprison);

            return ResponseEntity.ok(HTTPResponse.createSuccessResponse(
                    "또래 평균을 가져오는데 성공했습니다.",response, authInfo));

        }catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HTTPResponse.createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(HTTPResponse.createErrorResponse("또래 평균을 가져오는 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }
}

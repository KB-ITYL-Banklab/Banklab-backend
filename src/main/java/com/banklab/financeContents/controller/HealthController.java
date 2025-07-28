package com.banklab.financeContents.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API 헬스체크 컨트롤러
 * 프론트엔드에서 백엔드 API 상태를 확인하기 위한 헬스체크 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(tags = "시스템 헬스체크 API")
public class HealthController {

    @GetMapping("/health")
    @ApiOperation(value = "API 서버 헬스체크")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            log.info("🔍 API 헬스체크 요청");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("message", "API 서버가 정상적으로 실행 중입니다");
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "BankLab Finance API");
            
            log.info("✅ API 헬스체크 응답: 정상");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ API 헬스체크 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("message", "API 서버에 문제가 발생했습니다");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
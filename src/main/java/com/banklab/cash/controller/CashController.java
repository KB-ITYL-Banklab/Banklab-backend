package com.banklab.cash.controller;

import com.banklab.cash.service.CashService;
import com.banklab.cash.dto.CashDTO;
import com.banklab.common.redis.RedisService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Api(tags = "현금 관리 API")
public class CashController {

    private final CashService cashService;
    private final LoginUserProvider loginUserProvider;
    private final RedisService redisService;

    /**
     * 로그인한 사용자 정보 추출 및 검증
     */
    private Map<String, Object> extractAuthInfo() {
        Long memberId = loginUserProvider.getLoginMemberId();
        String email = loginUserProvider.getLoginEmail();


        if (memberId == null || email == null) {
            throw new SecurityException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("memberId", memberId);
        authInfo.put("email", email);
        return authInfo;
    }

    /**
     * 표준화된 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String message, Object data, Map<String, Object> authInfo) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("memberId", authInfo.get("memberId"));
        response.put("email", authInfo.get("email"));
        return response;
    }

    /**
     * 표준화된 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", errorCode);
        return response;
    }

    /**
     * 현금 정보 조회
     */
    @GetMapping
    @ApiOperation(value = "현금 정보 조회", notes = "로그인한 사용자의 보유 현금을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getCash() {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("현금 정보 조회 - email: {}, memberId: {}", email, memberId);

            CashDTO cashDTO = cashService.getCashByMemberId(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("cash", cashDTO);

            return ResponseEntity.ok(createSuccessResponse("현금 정보 조회 완료", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("현금 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("현금 정보 조회 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 현금 금액 설정/수정
     */
    @PutMapping
    @ApiOperation(value = "현금 금액 설정", notes = "보유 현금 금액을 설정하거나 수정합니다.")
    public ResponseEntity<Map<String, Object>> setCash(
            @RequestBody CashRequest request
    ) {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("현금 금액 설정 - email: {}, memberId: {}, amount: {}",
                    email, memberId, request.getAmount());

            // 입력값 검증
            if (request.getAmount() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("현금 금액이 입력되지 않았습니다.", "INVALID_INPUT"));
            }

            if (request.getAmount() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("현금 금액은 음수일 수 없습니다.", "INVALID_AMOUNT"));
            }

            CashDTO updatedCash = cashService.setCashAmount(memberId, request.getAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("cash", updatedCash);

            return ResponseEntity.ok(createSuccessResponse("현금 금액이 설정되었습니다.", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (IllegalArgumentException e) {
            log.error("입력값 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage(), "INVALID_INPUT"));

        } catch (Exception e) {
            log.error("현금 설정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("현금 설정 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 현금 업데이트 요청 DTO
     */
    @Data
    public static class CashRequest {
        private Long amount;
    }

}

package com.banklab.transaction.controller;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.security.util.LoginUserProvider;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.dto.response.TransactionDetailDTO;
import com.banklab.transaction.service.AsyncTransactionService;
import com.banklab.transaction.service.TransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Log4j2
@Api(tags = "거래 내역 API")
public class TransactionApiController {

    private final LoginUserProvider loginUserProvider;
    private final AsyncTransactionService asyncTransactionService;
    private final TransactionService transactionService;
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


    @PostMapping("/transaction-list")
    @ApiOperation(value = "CODEF 수시입출금 내역 API 호출", notes = "사용자와 연동된 계좌 거래 내역 조회")
    public ResponseEntity<Map<String, Object>> getTransactionList(
            @RequestBody(required = false) TransactionRequestDto dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("거래 내역 연동 시작 - email: {}, memberId: {}", email, memberId);

            // 2. 거래 내역 조회 및 DB 저장 - 비동기 처리
            asyncTransactionService.getTransactions(memberId, dto);

            return ResponseEntity.ok(createSuccessResponse("거래 내역 저장이 완료되었습니다.", response, authInfo));
        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("거래 내역 연동 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("거래 내역 저장 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/summary/status")
    public ResponseEntity<Map<String, Object>> checkLinkStatus(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request
    ) {
        Map<String, Object> authInfo = extractAuthInfo();
        Long memberId = (Long) authInfo.get("memberId");

        String accountNumber = requestBody.get("accountNumber");
        String key = RedisKeyUtil.transaction(memberId, accountNumber);

        String status = redisService.get(key);
        Map<String, Object> response = new HashMap<>();
        response.put("status", status != null ? status : "NOT_STARTED");


        return ResponseEntity.ok(response);
    }


    @GetMapping("/summary")
    @ApiOperation(value = "소비 분석 페이지 데이터 호출", notes = "사전 집계 테이블에서 data 호출하기")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("집계 테이블 GET 시작 - email: {}, memberId: {}", email, memberId);

            log.info("start - end {}-{}",startDate, endDate);
            // 2. 집계 테이블에 소비 분석 데이터 조회
            SummaryDTO summary = transactionService.getSummary(memberId, startDate, endDate);
            response.put("summaries", summary.getAccountSummaries());
            log.info("집계 테이블 조회 성공: - 거래 내역 개수: {}", summary.getAccountSummaries().get(0));

            return ResponseEntity.ok(createSuccessResponse("집계 테이블 조회에 성공했습니다.", response, authInfo));
        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));
        } catch (Exception e) {
            log.error("집계 테이블 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("거래 내역 저장 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    @GetMapping("/summary/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getThisByCategoryId(
            @PathVariable("categoryId") long categoryId,
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ){
        Map<String, Object> response = new HashMap<>();

        // JWT 토큰에서 빼오기
        Map<String, Object> authInfo = extractAuthInfo();
        Long memberId = (Long) authInfo.get("memberId");

        try{
            List<TransactionDetailDTO> results = transactionService.getTransactionDetailsByCategoryId(memberId, categoryId, startDate, endDate);
            response.put("transactions",results);
            return ResponseEntity.ok(createSuccessResponse("카테고리별 거래 내역 조회에 성공했습니다.", response, authInfo));
        }catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));
        } catch (Exception e) {
            log.error("집계 테이블 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("거래 내역 저장 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

}

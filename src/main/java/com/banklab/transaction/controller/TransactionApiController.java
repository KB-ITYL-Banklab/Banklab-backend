package com.banklab.transaction.controller;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.security.util.JwtProcessor;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.service.TransactionService;
import com.banklab.transaction.service.TransactionServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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

    private final JwtProcessor jwtProcessor;
    private final TransactionService transactionService;

    /**
     * HTTP 요청에서 JWT 토큰 추출 & 검증하여 사용자 정보 반환
     */
    private Map<String, Object> extractAuthInfo(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new SecurityException("인증이 필요합니다.");
        }
        String token = bearerToken.substring(7);
        if (!jwtProcessor.validateToken(token)) {
            throw new SecurityException("유효하지 않은 토큰입니다.");
        }

        try {
            Long memberId = jwtProcessor.getMemberId(token);
            String username = jwtProcessor.getUsername(token);

            if (memberId == null) {
                throw new SecurityException("토큰에 사용자 정보가 없습니다. 다시 로그인해주세요.");
            }

            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("memberId", memberId);
            authInfo.put("username", username);
            return authInfo;

        } catch (Exception e) {
            log.error("토큰 처리 중 오류 발생: {}", e.getMessage());
            throw new SecurityException("인증 처리 중 오류가 발생했습니다.");
        }
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
        response.put("username", authInfo.get("username"));
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
            HttpServletRequest request,
            @RequestBody(required = false) TransactionRequestDto dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("거래 내역 연동 시작 - username: {}, memberId: {}", username, memberId);

            // 2. 거래 내역 조회 및 DB 저장
            int savedTrHis = transactionService.getTransactions(memberId, dto);

            response.put("savedTransactions", savedTrHis);

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

    @GetMapping("/summary")
    @ApiOperation(value = "소비 분석 페이지 데이터 호출", notes = "사전 집계 테이블에서 data 호출하기")
    public ResponseEntity<Map<String, Object>> getSummary(
            HttpServletRequest request,
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("집계 테이블 GET 시작 - username: {}, memberId: {}", username, memberId);

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

    /** getSumamry 통합 전 코드
     @GetMapping("/monthly-summary") public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
     @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
     @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
     ) {
     return ResponseEntity.ok(transactionService.getMonthlySummary(startDate,endDate, account));
     }

     @GetMapping("/daily-summary") public ResponseEntity<List<DailyExpenseDTO>> getDailySummary(
     @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
     @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
     @RequestParam("account") String account
     ) {
     return ResponseEntity.ok(transactionService.getDailyExpense(startDate,endDate, account));
     }

     @GetMapping("/category") public List<CategoryExpenseDTO> getCategoryExpenses(
     @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
     @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
     @RequestParam("account") String account
     ) {

     // 기본값 설정: 오늘이 포함된 달의 시작일과 종료일
     LocalDate now = LocalDate.now();
     if (startDate == null) {
     startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
     }
     if (endDate == null) {
     endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
     }

     return transactionService.getCategoryExpense(startDate, endDate,account);
     }
     */


}

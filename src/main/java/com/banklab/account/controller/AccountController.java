package com.banklab.account.controller;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import com.banklab.account.dto.AccountRequestDTO;
import com.banklab.account.service.AccountResponse;
import com.banklab.account.service.AccountService;
import com.banklab.codef.service.RequestConnectedId;
import com.banklab.security.util.JwtProcessor;
import com.banklab.transaction.service.TransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Log4j2
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Api(tags = "계좌 관리 API")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final JwtProcessor jwtProcessor;

    /**
     * HTTP 요청에서 JWT 토큰을 추출하고 검증한 후, 사용자 정보를 반환
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

    /**
     * 은행 계좌 연동
     */
    @PostMapping("/link")
    @ApiOperation(value = "은행 계좌 연동", notes = "은행 로그인 정보로 계좌를 연동하고 DB에 저장.")
    public ResponseEntity<Map<String, Object>> linkAccount(
            HttpServletRequest request,
            @RequestBody AccountRequestDTO accountRequest
    ) {

        Map<String, Object> response = new HashMap<>();

        try {
            // JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("계좌 연동 시작 - username: {}, memberId: {}, bankCode: {}", username, memberId, accountRequest.getBankCode());

            // 1. 커넥티드 아이디 발급
            String userConnectedId = RequestConnectedId.createConnectedId(
                    accountRequest.getBankId(),
                    accountRequest.getBankPassword(),
                    accountRequest.getBankCode());

            // 2. 커넥티드 아이디로 계좌 정보 조회 및 DB 저장
            List<AccountVO> accountList = AccountResponse.requestAccounts(memberId, accountRequest.getBankCode(), userConnectedId);

            
            //3. 계좌 거래 내역 조회 API 호출
            transactionService.getTransactions(memberId, null);
            int savedCount = accountService.saveAccounts(accountList);

            // 3. 저장된 계좌 정보 조회하여 반환
            List<AccountDTO> accountDTOList = accountService.getUserAccounts(memberId);
            response.put("connectedId", userConnectedId);
            response.put("savedCount", savedCount);
            response.put("accounts", accountDTOList);

            return ResponseEntity.ok(createSuccessResponse("계좌 연동이 완료되었습니다.", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("계좌 연동 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("계좌 연동 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 사용자 계좌 목록 조회
     */
    @GetMapping("/list")
    @ApiOperation(value = "계좌 목록 조회", notes = "사용자의 연동된 계좌 목록을 조회.")
    public ResponseEntity<Map<String, Object>> getUserAccounts(HttpServletRequest request) {
        try {
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("계좌 목록 조회 - username: {}, memberId: {}", username, memberId);

            List<AccountDTO> accountList = accountService.getUserAccounts(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("accounts", accountList);
            response.put("count", accountList.size());

            return ResponseEntity.ok(createSuccessResponse("계좌 목록 조회 완료", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("계좌 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("계좌 목록 조회 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 계좌 잔액 새로고침
     */
    @PutMapping("/refresh")
    @ApiOperation(value = "계좌 잔액 새로고침", notes = "커넥티드 아이디로 계좌 잔액을 새로고침.")
    public ResponseEntity<Map<String, Object>> refreshAccountBalance(
            HttpServletRequest request,
            @RequestBody AccountRequestDTO accountRequest
    ) {
        try {
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("계좌 잔액 새로고침 - username: {}, memberId: {}, bankCode: {}",
                    username, memberId, accountRequest.getBankCode());

            // 권한 검증
            if (!accountService.isConnectedIdOwner(memberId, accountRequest.getConnectedId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("해당 계좌에 대한 권한이 없습니다.", "UNAUTHORIZED_ACCESS"));
            }

            // 잔액 새로고침
            accountService.refreshAccountBalance(memberId, accountRequest.getBankCode(), accountRequest.getConnectedId());
            List<AccountDTO> accountList = accountService.getUserAccounts(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("accounts", accountList);

            return ResponseEntity.ok(createSuccessResponse("계좌 잔액 새로고침 완료", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("계좌 잔액 새로고침 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("계좌 잔액 새로고침 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 계좌 연동 해제
     */
    @DeleteMapping("/unlink")
    @ApiOperation(value = "계좌 연동 해제", notes = "로그인한 사용자의 커넥티드 아이디를 삭제하고 계좌 연동을 해제.")
    public ResponseEntity<Map<String, Object>> unlinkAccount(
            HttpServletRequest request,
            @RequestBody AccountRequestDTO accountRequest
    ) {
        try {
            Map<String, Object> authInfo = extractAuthInfo(request);
            Long memberId = (Long) authInfo.get("memberId");
            String username = (String) authInfo.get("username");

            log.info("계좌 연동 해제 - username: {}, memberId: {}, bankCode: {}",
                    username, memberId, accountRequest.getBankCode());

            // 권한 검증
            if (!accountService.isConnectedIdOwner(memberId, accountRequest.getConnectedId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("해당 계좌에 대한 권한이 없습니다.", "UNAUTHORIZED_ACCESS"));
            }

            // 연동 해제
            boolean deleted = RequestConnectedId.deleteConnectedId(
                    accountRequest.getConnectedId(),
                    accountRequest.getBankCode()
            );

            if (deleted) {
                accountService.deleteAccount(memberId, accountRequest.getConnectedId());
                return ResponseEntity.ok(createSuccessResponse("계좌 연동 해제가 완료되었습니다.", null, authInfo));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("커넥티드 아이디 삭제에 실패했습니다.", "DELETE_FAILED"));
            }

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("계좌 연동 해제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("계좌 연동 해제 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }
}
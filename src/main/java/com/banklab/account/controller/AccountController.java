package com.banklab.account.controller;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import com.banklab.account.service.AccountResponse;
import com.banklab.account.service.AccountService;
import com.banklab.codef.service.RequestConnectedId;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 은행 계좌 연동
     * 1. 은행 로그인 정보로 커넥티드 아이디 발급
     * 2. 커넥티드 아이디로 계좌 정보 조회
     * 3. 계좌 정보를 DB에 저장
     */
    @PostMapping("/link")
    @ApiOperation(value = "은행 계좌 연동", notes = "은행 로그인 정보로 계좌를 연동하고 DB에 저장.")
    public ResponseEntity<Map<String, Object>> linkAccount(
            @RequestParam String userId,
            @RequestParam String bankCode,
            @RequestParam String bankId,
            @RequestParam String bankPassword
    ) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("계좌 연동 시작 - userId: {}, bankCode: {}", userId, bankCode);
            
            // 1. 커넥티드 아이디 발급
            String userConnectedId = RequestConnectedId.createConnectedId(bankId, bankPassword, bankCode);
            log.info("커넥티드 아이디 발급 완료: {}", userConnectedId);
            
            // 2. 커넥티드 아이디로 계좌 정보 조회
            List<AccountVO> accountList = AccountResponse.requestAccounts(userId, bankCode, userConnectedId);
            log.info("계좌 정보 조회 완료 - 계좌 수: {}", accountList.size());
            
            // 3. 계좌 정보를 DB에 저장
            int savedCount = accountService.saveAccounts(accountList);
            log.info("계좌 정보 저장 완료 - 저장된 계좌 수: {}", savedCount);
            
            // 4. 저장된 계좌 정보 조회하여 반환
            List<AccountDTO> accountDTOList = accountService.getUserAccounts(userId);
            
            response.put("success", true);
            response.put("message", "계좌 연동이 완료되었습니다.");
            response.put("connectedId", userConnectedId);
            response.put("savedCount", savedCount);
            response.put("accounts", accountDTOList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("계좌 연동 중 오류 발생", e);
            
            response.put("success", false);
            response.put("message", "계좌 연동 중 오류가 발생했습니다: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자 계좌 목록 조회
     */
    @GetMapping("/list")
    @ApiOperation(value = "계좌 목록 조회", notes = "사용자의 연동된 계좌 목록을 조회.")
    public ResponseEntity<Map<String, Object>> getUserAccounts(@RequestParam String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<AccountDTO> accountList = accountService.getUserAccounts(userId);
            
            response.put("success", true);
            response.put("message", "계좌 목록 조회 완료");
            response.put("accounts", accountList);
            response.put("count", accountList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("계좌 목록 조회 중 오류 발생", e);
            
            response.put("success", false);
            response.put("message", "계좌 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 계좌 잔액 새로고침
     */
    @PutMapping("/refresh")
    @ApiOperation(value = "계좌 잔액 새로고침", notes = "커넥티드 아이디로 계좌 잔액을 새로고침.")
    public ResponseEntity<Map<String, Object>> refreshAccountBalance(
            @RequestParam String userId,
            @RequestParam String bankCode,
            @RequestParam String connectedId
    ) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            accountService.refreshAccountBalance(userId, bankCode, connectedId);
            
            // 갱신된 계좌 정보 조회
            List<AccountDTO> accountList = accountService.getUserAccounts(userId);
            
            response.put("success", true);
            response.put("message", "계좌 잔액 새로고침 완료");
            response.put("accounts", accountList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("계좌 잔액 새로고침 중 오류 발생", e);
            
            response.put("success", false);
            response.put("message", "계좌 잔액 새로고침 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 계좌 연동 해제
     */
    @DeleteMapping("/unlink")
    @ApiOperation(value = "계좌 연동 해제", notes = "커넥티드 아이디를 삭제하고 계좌 연동을 해제.")
    public ResponseEntity<Map<String, Object>> unlinkAccount(
            @RequestParam String userId,
            @RequestParam String bankCode,
            @RequestParam String connectedId
    ) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 커넥티드 아이디 삭제 (CODEF API 호출)
            boolean deleted = RequestConnectedId.deleteConnectedId(connectedId, bankCode);
            
            if (deleted) {
                // 2. DB에서 계좌 정보 삭제
                accountService.deleteAccount(userId, connectedId);
                
                response.put("success", true);
                response.put("message", "계좌 연동 해제가 완료되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "커넥티드 아이디 삭제에 실패했습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("계좌 연동 해제 중 오류 발생", e);
            
            response.put("success", false);
            response.put("message", "계좌 연동 해제 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

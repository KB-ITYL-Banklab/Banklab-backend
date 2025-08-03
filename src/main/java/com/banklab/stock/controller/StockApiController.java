package com.banklab.stock.controller;

import com.banklab.codef.service.RequestConnectedId;
import com.banklab.common.redis.RedisService;
import com.banklab.security.service.LoginUserProvider;
import com.banklab.stock.domain.StockVO;
import com.banklab.stock.dto.StockRequestDTO;
import com.banklab.stock.service.StockResponse;
import com.banklab.stock.service.StockService;
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
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Api(tags = "증권 관리 API")
public class StockApiController {

    private final StockService stockService;
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
     * 증권계좌 연동
     */
    @PostMapping("/link")
    @ApiOperation(value = "증권계좌 연동", notes = "증권사 로그인 정보로 계좌를 연동하고 보유종목을 DB에 저장.")
    public ResponseEntity<Map<String, Object>> linkStock(
            @RequestBody StockRequestDTO stockRequest
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // JWT 토큰에서 인증 정보 추출
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("증권계좌 연동 시작 - email: {}, memberId: {}, stockCode: {}",
                    email, memberId, stockRequest.getStockCode());

            // 1. 커넥티드 아이디 발급
            String userConnectedId = RequestConnectedId.createConnectedId(
                    stockRequest.getStockId(),
                    stockRequest.getStockPassword(),
                    stockRequest.getStockCode(),
                    "ST",
                    "A");

            // 2. 커넥티드 아이디로 보유종목 정보 조회 및 DB 저장
            List<StockVO> stockList = StockResponse.requestStocks(
                    memberId, stockRequest.getStockCode(), userConnectedId, stockRequest.getAccount(), stockRequest.getAccountPassword());
            stockService.saveStocks(stockList);

            // 3. 저장된 보유종목 정보 조회하여 반환
            List<StockVO> userStocks = stockService.getUserStocks(memberId);
            response.put("connectedId", userConnectedId);
            response.put("savedCount", stockList.size());
            response.put("stocks", userStocks);

            return ResponseEntity.ok(createSuccessResponse("증권계좌 연동이 완료되었습니다.", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("증권계좌 연동 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("증권계좌 연동 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 사용자 종합자산 보유종목 목록 조회
     */
    @GetMapping("/list")
    @ApiOperation(value = "보유종목 목록 조회", notes = "사용자의 연동된 증권계좌 보유종목을 조회.")
    public ResponseEntity<Map<String, Object>> getUserStocks() {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("보유종목 목록 조회 - email: {}, memberId: {}", email, memberId);

            List<StockVO> stockList = stockService.getUserStocks(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("stocks", stockList);
            response.put("count", stockList.size());

            return ResponseEntity.ok(createSuccessResponse("보유종목 목록 조회 완료", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("보유종목 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("보유종목 목록 조회 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 보유종목 정보 새로고침
     */
    @PutMapping("/refresh")
    @ApiOperation(value = "보유종목 정보 새로고침", notes = "커넥티드 아이디로 보유종목 정보를 새로고침.")
    public ResponseEntity<Map<String, Object>> refreshUserStocks(
            @RequestBody StockRequestDTO stockRequest
    ) {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("보유종목 정보 새로고침 - email: {}, memberId: {}, stockCode: {}",
                    email, memberId, stockRequest.getStockCode());

            // 보유종목 새로고침
            stockService.refreshUserStocks(
                    memberId,
                    stockRequest.getStockCode(),
                    stockRequest.getConnectedId(),
                    stockRequest.getAccount()
            );

            List<StockVO> stockList = stockService.getUserStocks(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("stocks", stockList);

            return ResponseEntity.ok(createSuccessResponse("보유종목 정보 새로고침 완료", response, authInfo));

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("보유종목 정보 새로고침 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("보유종목 정보 새로고침 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    /**
     * 증권계좌 연동 해제
     */
    @DeleteMapping("/unlink")
    @ApiOperation(value = "증권계좌 연동 해제", notes = "로그인한 사용자의 커넥티드 아이디를 삭제하고 증권계좌 연동을 해제.")
    public ResponseEntity<Map<String, Object>> unlinkStock(
            @RequestBody StockRequestDTO stockRequest
    ) {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");
            String email = (String) authInfo.get("email");

            log.info("증권계좌 연동 해제 - email: {}, memberId: {}, stockCode: {}, account: {}",
                    email, memberId, stockRequest.getStockCode(), stockRequest.getAccount());

            // 연동 해제
            boolean deleted = RequestConnectedId.deleteConnectedId(
                    stockRequest.getConnectedId(),
                    stockRequest.getStockCode(),
                    "ST",
                    "A"
            );

            if (deleted) {
                stockService.disconnectUserStocks(memberId, stockRequest.getConnectedId());
                return ResponseEntity.ok(createSuccessResponse("증권계좌 연동 해제가 완료되었습니다.", null, authInfo));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("커넥티드 아이디 삭제에 실패했습니다.", "DELETE_FAILED"));
            }

        } catch (SecurityException e) {
            log.error("인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage(), "AUTHENTICATION_ERROR"));

        } catch (Exception e) {
            log.error("증권계좌 연동 해제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("증권계좌 연동 해제 중 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

}

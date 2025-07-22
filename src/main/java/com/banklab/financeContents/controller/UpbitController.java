package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.BitcoinTickerDTO;
import com.banklab.financeContents.service.UpbitApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class UpbitController
 * @description 업비트(Upbit) API와 연동하여 비트코인 시세 정보를 제공하는 REST 컨트롤러입니다.
 * - Swagger를 통해 API 문서를 자동으로 생성하고 명세를 관리합니다.
 */
@RestController
@RequestMapping("/api/financeContents/upbit")
@Api(tags = "업비트 암호화폐 시세 API") // Swagger UI에 표시될 API 그룹 이름
public class UpbitController {

    // SLF4J를 이용한 로거(Logger) 인스턴스 생성
    private static final Logger logger = LoggerFactory.getLogger(UpbitController.class);

    // 비즈니스 로직을 처리하는 서비스 레이어(UpbitApiService)를 의존성 주입(DI) 받습니다.
    @Autowired
    private UpbitApiService upbitApiService;

    /**
     * @method getBitcoinTicker
     * @description 업비트 API를 통해 비트코인(KRW-BTC)의 전체 시세 정보를 조회합니다.
     */
    @GetMapping("/bitcoin")
    @ApiOperation(value = "비트코인 시세 조회", notes = "업비트에서 비트코인(KRW-BTC)의 상세 시세 정보를 조회합니다.")
    public ResponseEntity<?> getBitcoinTicker() {
        try {
            // 요청 로그 기록
            logger.info("비트코인 시세 조회 요청");

            // 서비스 레이어를 통해 비트코인 시세 정보 조회
            BitcoinTickerDTO ticker = upbitApiService.getBitcoinTicker();

            // ticker 객체가 null이 아닌지 확인하여 API 호출 성공 여부 판단
            if (ticker != null) {
                // 성공 로그 기록
                logger.info("비트코인 시세 조회 성공: {} KRW", ticker.getTradePrice());
                // 조회된 시세 정보(DTO)와 HTTP 200 (OK) 상태 반환
                return ResponseEntity.ok(ticker);
            } else {
                // API 연동 실패 로그 기록
                logger.warn("비트코인 시세 조회 실패");
                // 클라이언트에게 전달할 에러 응답 생성
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "비트코인 시세 정보를 가져올 수 없습니다.");
                errorResponse.put("message", "업비트 API 호출에 실패했습니다.");
                // 에러 응답과 HTTP 503 (Service Unavailable) 상태 반환
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

        } catch (Exception e) {
            // 예기치 않은 서버 오류 발생 시 로그 기록
            logger.error("비트코인 시세 조회 중 오류 발생: {}", e.getMessage(), e);
            // 클라이언트에게 전달할 에러 응답 생성
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류");
            errorResponse.put("message", e.getMessage());
            // 에러 응답과 HTTP 500 (Internal Server Error) 상태 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * @method getBitcoinPrice
     * @description 비트코인(KRW-BTC)의 현재 체결 가격만 간편하게 조회합니다.
     */
    @GetMapping("/bitcoin/price")
    @ApiOperation(value = "비트코인 현재가 조회", notes = "비트코인(KRW-BTC) 현재가만 간단하게 조회합니다.")
    public ResponseEntity<?> getBitcoinPrice() {
        try {
            logger.info("비트코인 현재가 조회 요청");

            // 서비스 레이어를 통해 비트코인 현재가(Double) 조회
            Double price = upbitApiService.getBitcoinPrice();

            if (price != null) {
                // 성공 시 반환할 응답 데이터를 Map으로 구성
                Map<String, Object> response = new HashMap<>();
                response.put("market", "KRW-BTC"); // 마켓 코드
                response.put("price", price); // 현재가
                response.put("currency", "KRW"); // 기준 통화
                response.put("timestamp", System.currentTimeMillis()); // 응답 시점의 타임스탬프

                logger.info("비트코인 현재가 조회 성공: {} KRW", price);
                return ResponseEntity.ok(response);
            } else {
                // API 연동 실패 시 에러 응답 생성
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "비트코인 현재가를 가져올 수 없습니다.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

        } catch (Exception e) {
            // 서버 내부 오류 처리
            logger.error("비트코인 현재가 조회 중 오류 발생: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * @method getMultipleTickers
     * @description 쉼표(,)로 구분된 여러 암호화폐 마켓 코드의 시세 정보를 한 번에 조회합니다.
     * @param markets 조회할 암호화폐 마켓 코드 문자열 (예: "KRW-BTC,KRW-ETH,KRW-XRP")
     * @return ResponseEntity<?> - 성공 시 시세 정보 DTO 리스트와 HTTP 200 (OK) 상태 코드를 반환합니다.
     * - 실패 시 에러 메시지와 해당 HTTP 상태 코드를 반환합니다.
     */
    @GetMapping("/tickers")
    @ApiOperation(value = "다중 암호화폐 시세 조회", notes = "여러 암호화폐의 시세 정보를 한 번에 조회합니다.")
    public ResponseEntity<?> getMultipleTickers(
            @ApiParam(value = "암호화폐 마켓 코드 (쉼표로 구분)", example = "KRW-BTC,KRW-ETH,KRW-XRP", required = true)
            @RequestParam(defaultValue = "KRW-BTC,KRW-ETH,KRW-XRP") String markets) {
        try {
            logger.info("다중 암호화폐 시세 조회 요청: {}", markets);

            // 서비스 레이어를 통해 여러 암호화폐 시세 정보 조회
            List<BitcoinTickerDTO> tickers = upbitApiService.getMultipleTickers(markets);

            // 조회된 리스트가 비어있지 않은지 확인
            if (tickers != null && !tickers.isEmpty()) {
                logger.info("{}개 암호화폐 시세 조회 성공", tickers.size());
                return ResponseEntity.ok(tickers);
            } else {
                // API 연동 실패 또는 데이터 없음
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "암호화폐 시세 정보를 가져올 수 없습니다.");
                errorResponse.put("markets", markets); // 어떤 마켓에 대한 요청이었는지 명시
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

        } catch (Exception e) {
            // 서버 내부 오류 처리
            logger.error("다중 암호화폐 시세 조회 중 오류 발생: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * @method checkApiHealth
     * @description 외부 API(업비트)의 현재 연결 상태를 확인하는 Health Check 엔드포인트입니다.
     */
    @GetMapping("/health")
    @ApiOperation(value = "API 상태 확인", notes = "업비트 API와의 연결 상태를 확인합니다.")
    public ResponseEntity<Map<String, Object>> checkApiHealth() {
        try {
            logger.info("업비트 API 상태 확인 요청");

            // 서비스 레이어를 통해 API 사용 가능 여부 확인
            boolean isAvailable = upbitApiService.isApiAvailable();

            // 상태 확인 결과를 담을 응답 Map 생성
            Map<String, Object> response = new HashMap<>();
            response.put("status", isAvailable ? "UP" : "DOWN"); // API 상태
            response.put("service", "Upbit API"); // 대상 서비스
            response.put("timestamp", System.currentTimeMillis());
            response.put("available", isAvailable); // boolean 형태의 상태 값

            // API 상태에 따라 다른 HTTP 상태 코드 설정
            HttpStatus status = isAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

            logger.info("업비트 API 상태: {}", isAvailable ? "정상" : "불가능");
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            // Health Check 중 예외 발생 시
            logger.error("API 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("service", "Upbit API");
            response.put("timestamp", System.currentTimeMillis());
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
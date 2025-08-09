package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.*;
import com.banklab.financeContents.service.FinanceQuizService;
import com.banklab.security.util.LoginUserProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//경제 퀴즈 컨트롤러
@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(originPatterns = "*")
@Api(tags = "Finance Quiz", description = "경제 퀴즈 API")
public class FinanceQuizController {

    @Autowired
    private FinanceQuizService financeQuizService;
    
    @Autowired
    private LoginUserProvider loginUserProvider;

    @ApiOperation(value = "헬스 체크", notes = "서비스 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Finance Quiz Service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "모든 퀴즈 조회", notes = "모든 경제 퀴즈를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 조회됨"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/all")
    public ResponseEntity<List<FinanceQuizDTO>> getAllQuizzes() {
        try {
            List<FinanceQuizDTO> quizzes = financeQuizService.getAllQuizzes();
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    @ApiOperation(value = "오늘의 퀴즈 조회 (랜덤)", notes = "오늘 날짜 기준으로 모든 사용자에게 동일한 3문제를 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 조회됨"),
            @ApiResponse(code = 400, message = "잘못된 요청"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/random")
    public ResponseEntity<QuizResponseDTO> getRandomQuizzes(
            @ApiParam(value = "가져올 퀴즈 개수 (무시됨, 항상 3문제)", required = false)
            @RequestParam(defaultValue = "3") int count,
            @ApiParam(value = "퀴즈 타입 (무시됨, 오늘의 퀴즈 고정)", required = false)
            @RequestParam(required = false) String type) {
        try {
            // 오늘의 퀴즈 3문제 반환 (파라미터 무시)
            List<FinanceQuizDTO> quizzes = financeQuizService.getTodayQuizzes();
            QuizResponseDTO response = new QuizResponseDTO(quizzes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "오늘의 퀴즈 조회", notes = "오늘 날짜 기준으로 모든 사용자에게 동일한 3문제를 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 조회됨"),
            @ApiResponse(code = 403, message = "이미 오늘 퀴즈를 완료함"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayQuizzes() {
        try {
            // 로그인한 사용자 ID 가져오기 (선택사항)
            Long memberId = null;
            try {
                memberId = loginUserProvider.getLoginMemberId();
            } catch (Exception e) {
                // 비로그인 사용자의 경우 퀴즈만 제공
            }
            
            Map<String, Object> response = new HashMap<>();
            
            // 오늘의 퀴즈 문제들 가져오기 (완료 여부와 상관없이)
            List<FinanceQuizDTO> todayQuizzes = financeQuizService.getTodayQuizzes();
            
            // 로그인 사용자의 경우 오늘 퀴즈 완료 여부 확인
            if (memberId != null && financeQuizService.hasUserSolvedTodayQuiz(memberId)) {
                // DB에서 오늘의 퀴즈 결과 조회
                DailyQuizResultDTO todayResult = financeQuizService.getTodayQuizResult(memberId);
                
                response.put("success", false);
                response.put("message", "오늘은 이미 퀴즈를 완료하셨습니다. 내일 다시 도전해주세요!");
                response.put("alreadySolved", true);
                response.put("quizzes", todayQuizzes); // 퀴즈 문제들 제공
                
                if (todayResult != null) {
                    // DB에서 가져온 상세 결과 포함
                    response.put("quizResult", todayResult);
                }
                
                return ResponseEntity.status(403).body(response);
            }
            
            response.put("success", true);
            response.put("message", "오늘의 퀴즈를 성공적으로 조회했습니다.");
            response.put("quizzes", todayQuizzes);
            response.put("alreadySolved", false);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== GET /api/quiz/today 오류 발생 ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @ApiOperation(value = "퀴즈 답변 검증", notes = "사용자의 답변을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 검증됨"),
            @ApiResponse(code = 400, message = "잘못된 요청"),
            @ApiResponse(code = 404, message = "퀴즈를 찾을 수 없음"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @PostMapping("/check-answer")
    public ResponseEntity<QuizResultDTO> checkAnswer(@RequestBody QuizAnswerRequestDTO answerRequest) {
        try {
            QuizResultDTO result = financeQuizService.checkAnswer(answerRequest);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "일일 퀴즈 결과 처리", notes = "사용자의 일일 퀴즈 답안을 처리하고 포인트를 계산합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 처리됨"),
            @ApiResponse(code = 400, message = "잘못된 요청"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @PostMapping("/daily-result")
    public ResponseEntity<Map<String, Object>> processDailyQuizResults(@RequestBody DailyQuizRequestDTO request) {
        try {
            System.out.println("=== POST /api/quiz/daily-result 호출됨 ===");
            System.out.println("Received request: " + request);
            System.out.println("User Answer: " + request.getUserAnswer());
            System.out.println("User Answer Length: " + (request.getUserAnswer() != null ? request.getUserAnswer().length() : "null"));
            
            DailyQuizResultDTO result = financeQuizService.processDailyQuizResults(request);
            System.out.println("DB 저장 성공, 응답 데이터: " + result);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "퀴즈 결과가 성공적으로 저장되었습니다.");
            response.put("result", result);
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 이미 오늘 퀴즈를 완료한 경우
            System.out.println("IllegalStateException: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("alreadySolved", true);
            return ResponseEntity.status(403).body(errorResponse);
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "잘못된 요청입니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @ApiOperation(value = "퀴즈 통계 조회", notes = "퀴즈 개수 및 타입별 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 조회됨"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getQuizStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", financeQuizService.getTotalQuizCount());
            
            List<String> quizTypes = financeQuizService.getAvailableQuizTypes();
            Map<String, Integer> countByType = new HashMap<>();
            
            for (String type : quizTypes) {
                countByType.put(type, financeQuizService.getQuizCountByType(type));
            }
            
            statistics.put("countByType", countByType);
            statistics.put("availableTypes", quizTypes);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "사용자 퀴즈 통계 조회", notes = "로그인한 사용자의 퀴즈 통계를 조회합니다. (총 문제 수, 정답 수, 정답률, 총 포인트)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 조회됨"),
            @ApiResponse(code = 401, message = "인증되지 않은 사용자"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserQuizStats() {
        try {
            Long memberId = loginUserProvider.getLoginMemberId();
            if (memberId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            UserQuizStatsDTO stats = financeQuizService.getUserQuizStats(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 퀴즈 통계를 성공적으로 조회했습니다.");
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

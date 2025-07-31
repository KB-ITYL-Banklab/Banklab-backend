package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.*;
import com.banklab.financeContents.service.FinanceQuizService;
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
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/today")
    public ResponseEntity<QuizResponseDTO> getTodayQuizzes() {
        try {
            List<FinanceQuizDTO> todayQuizzes = financeQuizService.getTodayQuizzes();
            QuizResponseDTO response = new QuizResponseDTO(todayQuizzes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
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
    public ResponseEntity<DailyQuizResultDTO> processDailyQuizResults(@RequestBody DailyQuizRequestDTO request) {
        try {
            System.out.println("=== POST /api/quiz/daily-result 호출됨 ===");
            System.out.println("Received request: " + request);
            System.out.println("Member ID: " + request.getMemberId());
            System.out.println("User Answer: " + request.getUserAnswer());
            System.out.println("User Answer Length: " + (request.getUserAnswer() != null ? request.getUserAnswer().length() : "null"));
            
            DailyQuizResultDTO result = financeQuizService.processDailyQuizResults(request);
            System.out.println("DB 저장 성공, 응답 데이터: " + result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
}

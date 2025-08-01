package com.banklab.typetest.controller;

import com.banklab.security.util.JwtProcessor;
import com.banklab.typetest.domain.Question;
import com.banklab.typetest.service.TypeTestService;
import com.banklab.typetest.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.dto.QuestionsResponseDTO;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 유형검사 컨트롤러
 */
@RestController
@RequestMapping("api/typetest")
@RequiredArgsConstructor
public class TypeTestController {

    private final TypeTestService typeTestService;
    private final JwtProcessor jwtProcessor;

    /**
     * 투자 유형 검사를 위한 질문 조회 API
     * @return 질문 목록
     */
    @GetMapping("/questions")
    public ResponseEntity<QuestionsResponseDTO> getAllQuestions() {
        List<Question> questions = typeTestService.getAllQuestions();
        QuestionsResponseDTO response = QuestionsResponseDTO.builder()
                .questions(questions)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 유형검사를 제출합니다 (OK만 반환)
     * @param request 토큰 추출을 위한 request
     * @param payload 질문 답변
     * @return OK 메시지
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitAnswers(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        String token = JwtTokenUtil.extractToken(request);
        Long memberId = jwtProcessor.getMemberId(token);
        if (memberId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 토큰입니다."));
        }
        typeTestService.submitAnswersWithMemberId(payload, memberId);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    /**
     * 사용자 투자 유형 결과 및 추천상품 4개 반환
     * @param request 토큰 추출을 위한 request
     * @return 검사결과가 있는 경우, 사용자 투자유형과 추천 상품 4개 반환
     * @return 검사결과가 없는 경우, 검사 유도 메시지 반환
     */
    @GetMapping("/result")
    public ResponseEntity<TypeTestResultDTO> getTestResultByToken(HttpServletRequest request) {
        String token = JwtTokenUtil.extractToken(request);
        Long memberId = jwtProcessor.getMemberId(token);
        if (memberId == null) {
            return ResponseEntity.badRequest().body(TypeTestResultDTO.builder().message("유효하지 않은 토큰입니다.").build());
        }
        TypeTestResultDTO result = typeTestService.getTestResultByUserId(memberId);
        if (result == null || result.getInvestmentTypeId() == null) {
            return ResponseEntity.ok(TypeTestResultDTO.builder().message("검사 결과가 없습니다. 먼저 검사를 진행하세요.").build());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 투자유형에 따른 전체 상품(위험도별 매핑) 반환
     * @param request 토큰 추출을 위한 request
     * @return 투자유형, 위험도별 전체 상품 리스트
     */
    @GetMapping("/result/all")
    public ResponseEntity<TypeTestResultDTO> getAllProductsByType(HttpServletRequest request) {
        String token = JwtTokenUtil.extractToken(request);
        Long memberId = jwtProcessor.getMemberId(token);
        if (memberId == null) {
            return ResponseEntity.badRequest().body(TypeTestResultDTO.builder().message("유효하지 않은 토큰입니다.").build());
        }
        TypeTestResultDTO result = typeTestService.getAllProductsByType(memberId);
        if (result == null || result.getInvestmentTypeId() == null) {
            return ResponseEntity.ok(TypeTestResultDTO.builder().message("검사 결과가 없습니다. 먼저 검사를 진행하세요.").build());
        }
        return ResponseEntity.ok(result);
    }
}

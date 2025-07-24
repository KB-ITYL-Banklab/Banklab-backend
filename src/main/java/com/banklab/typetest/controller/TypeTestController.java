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

@RestController
@RequestMapping("api/typetest")
@RequiredArgsConstructor
public class TypeTestController {

    private final TypeTestService typeTestService;
    private final JwtProcessor jwtProcessor;

    @GetMapping("/questions")
    public ResponseEntity<QuestionsResponseDTO> getAllQuestions() {
        List<Question> questions = typeTestService.getAllQuestions();
        QuestionsResponseDTO response = QuestionsResponseDTO.builder()
                .questions(questions)
                .build();
        return ResponseEntity.ok(response);
    }

    // 질문 제출: JWT에서 memberId 추출해 userId로 사용
    @PostMapping("")
    public ResponseEntity<TypeTestResultDTO> submitAnswers(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        String token = JwtTokenUtil.extractToken(request);
        Long memberId = jwtProcessor.getMemberId(token);
        // memberId가 null이면 400 에러 반환
        if (memberId == null) {
            return ResponseEntity.badRequest().body(TypeTestResultDTO.builder().message("유효하지 않은 토큰입니다.").build());
        }
        return ResponseEntity.ok(typeTestService.submitAnswersWithMemberId(payload, memberId));
    }

    // 검사 결과 조회: JWT에서 memberId 추출해 userId로 사용
    @GetMapping("/result")
    public ResponseEntity<TypeTestResultDTO> getTestResultByToken(HttpServletRequest request) {
        String token = JwtTokenUtil.extractToken(request);
        Long memberId = jwtProcessor.getMemberId(token);
        // memberId가 null이면 안내 메시지 반환
        if (memberId == null) {
            System.out.println("member Id 가 null 입니다.");
        }
        // 서비스에서 결과가 없으면 안내 메시지 반환
        TypeTestResultDTO result = typeTestService.getTestResultByUserId(memberId);
        if (result == null || result.getInvestmentTypeId() == null) {
            return ResponseEntity.ok(TypeTestResultDTO.builder().message("검사 결과가 없습니다. 먼저 검사를 진행하세요.").build());
        }
        return ResponseEntity.ok(result);
    }

}

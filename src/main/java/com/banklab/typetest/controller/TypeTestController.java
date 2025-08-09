package com.banklab.typetest.controller;

import com.banklab.security.util.LoginUserProvider;
import com.banklab.typetest.domain.Question;
import com.banklab.typetest.service.TypeTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.dto.QuestionsResponseDTO;
import java.util.List;
import java.util.Map;

/**
 * 유형검사 컨트롤러
 */
@RestController
@RequestMapping("api/typetest")
@RequiredArgsConstructor
@Api(tags = "투자성향 검사 API", description = "사용자의 투자성향을 파악하고 맞춤형 금융상품을 추천")
public class TypeTestController {

    private final TypeTestService typeTestService;
    private final LoginUserProvider loginUserProvider;

    /**
     * 투자 유형 검사를 위한 질문 조회 API
     * @return 질문 목록
     */
    @GetMapping("/questions")
    @ApiOperation(value = "투자성향 검사 질문 목록 조회", notes = "투자성향 검사를 위한 모든 질문을 조회.")
    public ResponseEntity<QuestionsResponseDTO> getAllQuestions() {
        List<Question> questions = typeTestService.getAllQuestions();
        QuestionsResponseDTO response = QuestionsResponseDTO.builder()
                .questions(questions)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 유형검사를 제출합니다 (OK만 반환)
     * @param payload 질문 답변
     * @return OK 메시지
     */
    @PostMapping("/submit")
    @ApiOperation(value = "투자성향 검사 결과 제출", notes = "사용자가 응답한 투자성향 검사 답변을 제출하고 결과를 분석.")
    public ResponseEntity<TypeTestResultDTO> submitAnswers(
            @ApiParam(value = "투자성향 검사 답변 데이터")
            @RequestBody Map<String, Object> payload) {
        Long memberId = loginUserProvider.getLoginMemberId();
        TypeTestResultDTO result = typeTestService.submitAnswersWithMemberId(payload, memberId);
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 투자 유형 결과 및 추천상품 4개 반환
     * @return 검사결과가 있는 경우, 사용자 투자유형과 추천 상품 4개 반환
     * @return 검사결과가 없는 경우, 검사 유도 메시지 반환
     */
    @GetMapping("/result")
    @ApiOperation(value = "사용자 투자성향 결과 및 추천상품 조회", notes = "현재 로그인한 사용자의 투자성향 검사 결과와 맞춤 추천상품 4개를 조회.")
    public ResponseEntity<TypeTestResultDTO> getTestResult() {
        Long memberId = loginUserProvider.getLoginMemberId();
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
     * @return 투자유형, 위험도별 전체 상품 리스트
     */
    @GetMapping("/result/all")
    @ApiOperation(value = "투자성향별 전체 상품 목록 조회", notes = "사용자의 투자성향에 따른 위험도별 전체 금융상품을 조회.")
    public ResponseEntity<TypeTestResultDTO> getAllProductsByType() {
        Long memberId = loginUserProvider.getLoginMemberId();
        if (memberId == null) {
            return ResponseEntity.badRequest().body(TypeTestResultDTO.builder().message("유효하지 않은 토큰입니다.").build());
        }
        TypeTestResultDTO result = typeTestService.getAllProductsByType(memberId);
        if (result == null || result.getInvestmentTypeId() == null) {
            return ResponseEntity.ok(TypeTestResultDTO.builder().message("검사 결과가 없습니다. 먼저 검사를 진행하세요.").build());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 투자유형 반환
     */
    @GetMapping("/user-type")
    @ApiOperation(value = "사용자 투자성향 타입 조회", notes = "현재 로그인한 사용자의 투자성향 타입만 간단히 조회.")
    public ResponseEntity<TypeTestResultDTO> getUserInvestmentType() {
        Long memberId = loginUserProvider.getLoginMemberId();
        TypeTestResultDTO result = typeTestService.getUserInvestmentType(memberId);
        return ResponseEntity.ok(result);
    }
}

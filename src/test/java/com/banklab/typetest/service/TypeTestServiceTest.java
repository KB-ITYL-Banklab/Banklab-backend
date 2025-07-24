package com.banklab.typetest.service;

import com.banklab.typetest.domain.Question;
import com.banklab.typetest.dto.TypeTestResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("유형검사 서비스 테스트")
class TypeTestServiceTest {

    @Mock
    private TypeTestServiceImpl typeTestService;

    @BeforeEach
    void setUp() {
        // 테스트 설정이 필요한 경우 여기에 추가
    }

    @Test
    @DisplayName("모든 질문 조회 - 전체 질문 목록이 올바르게 반환되는지 테스트")
    void 모든_질문_조회시_전체_질문_목록이_올바르게_반환되는지() {
        // Given
        List<Question> expectedQuestions = Arrays.asList(
                createQuestion(1L, "투자 경험이 어느 정도이신가요?"),
                createQuestion(2L, "투자 손실을 어느 정도 감수할 수 있나요?"),
                createQuestion(3L, "투자 기간은 얼마나 생각하고 계신가요?"),
                createQuestion(4L, "투자 목적은 무엇인가요?"),
                createQuestion(5L, "경제 상황 변화에 대한 대응은?")
        );

        when(typeTestService.getAllQuestions()).thenReturn(expectedQuestions);

        // When
        List<Question> actualQuestions = typeTestService.getAllQuestions();

        // Then
        assertEquals(5, actualQuestions.size());
        assertEquals("투자 경험이 어느 정도이신가요?", actualQuestions.get(0).getQuestionText());
        assertEquals("투자 손실을 어느 정도 감수할 수 있나요?", actualQuestions.get(1).getQuestionText());
        assertEquals("투자 기간은 얼마나 생각하고 계신가요?", actualQuestions.get(2).getQuestionText());
        assertEquals("투자 목적은 무엇인가요?", actualQuestions.get(3).getQuestionText());
        assertEquals("경제 상황 변화에 대한 대응은?", actualQuestions.get(4).getQuestionText());
    }

    @Test
    @DisplayName("유형검사 제출 - 안정형 결과가 올바르게 반환되는지 테스트")
    void 안정형_답변_제출시_안정형_결과가_올바르게_반환되는지() {
        // Given
        Long memberId = 1L;
        Map<String, Object> conservativeAnswers = new HashMap<>();
        conservativeAnswers.put("answers", Arrays.asList(1, 1, 1, 1, 1)); // 모두 안정형 답변
        
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(1L)
                .investmentTypeName("안정형")
                .investmentTypeDesc("원금 보장을 중시하며 안전한 투자를 선호합니다.")
                .build();

        when(typeTestService.submitAnswersWithMemberId(eq(conservativeAnswers), eq(memberId)))
                .thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.submitAnswersWithMemberId(conservativeAnswers, memberId);

        // Then
        assertEquals(Long.valueOf(1L), actualResult.getInvestmentTypeId());
        assertEquals("안정형", actualResult.getInvestmentTypeName());
        assertTrue(actualResult.getInvestmentTypeDesc().contains("원금 보장"));
    }

    @Test
    @DisplayName("유형검사 제출 - 중립형 결과가 올바르게 반환되는지 테스트")
    void 중립형_답변_제출시_중립형_결과가_올바르게_반환되는지() {
        // Given
        Long memberId = 2L;
        Map<String, Object> moderateAnswers = new HashMap<>();
        moderateAnswers.put("answers", Arrays.asList(2, 2, 2, 2, 2)); // 모두 중립형 답변
        
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(2L)
                .investmentTypeName("중립형")
                .investmentTypeDesc("적당한 위험을 감수하며 안정적인 수익을 추구합니다.")
                .build();

        when(typeTestService.submitAnswersWithMemberId(eq(moderateAnswers), eq(memberId)))
                .thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.submitAnswersWithMemberId(moderateAnswers, memberId);

        // Then
        assertEquals(Long.valueOf(2L), actualResult.getInvestmentTypeId());
        assertEquals("중립형", actualResult.getInvestmentTypeName());
        assertTrue(actualResult.getInvestmentTypeDesc().contains("적당한 위험"));
    }

    @Test
    @DisplayName("유형검사 제출 - 공격형 결과가 올바르게 반환되는지 테스트")
    void 공격형_답변_제출시_공격형_결과가_올바르게_반환되는지() {
        // Given
        Long memberId = 3L;
        Map<String, Object> aggressiveAnswers = new HashMap<>();
        aggressiveAnswers.put("answers", Arrays.asList(3, 3, 3, 3, 3)); // 모두 공격형 답변
        
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(3L)
                .investmentTypeName("공격형")
                .investmentTypeDesc("높은 수익을 위해 높은 위험도 감수할 수 있습니다.")
                .build();

        when(typeTestService.submitAnswersWithMemberId(eq(aggressiveAnswers), eq(memberId)))
                .thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.submitAnswersWithMemberId(aggressiveAnswers, memberId);

        // Then
        assertEquals(Long.valueOf(3L), actualResult.getInvestmentTypeId());
        assertEquals("공격형", actualResult.getInvestmentTypeName());
        assertTrue(actualResult.getInvestmentTypeDesc().contains("높은 수익"));
    }

    @Test
    @DisplayName("혼합 답변 제출 - 혼합된 답변으로 적절한 유형이 결정되는지 테스트")
    void 혼합된_답변_제출시_적절한_유형이_결정되는지() {
        // Given
        Long memberId = 4L;
        Map<String, Object> mixedAnswers = new HashMap<>();
        mixedAnswers.put("answers", Arrays.asList(1, 2, 3, 2, 1)); // 혼합된 답변
        
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(2L)
                .investmentTypeName("중립형")
                .investmentTypeDesc("적당한 위험을 감수하며 안정적인 수익을 추구합니다.")
                .build();

        when(typeTestService.submitAnswersWithMemberId(eq(mixedAnswers), eq(memberId)))
                .thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.submitAnswersWithMemberId(mixedAnswers, memberId);

        // Then
        assertEquals(Long.valueOf(2L), actualResult.getInvestmentTypeId());
        assertEquals("중립형", actualResult.getInvestmentTypeName());
    }

    @Test
    @DisplayName("유형검사 결과 조회 - 기존 결과가 올바르게 조회되는지 테스트")
    void 기존_유형검사_결과가_올바르게_조회되는지() {
        // Given
        Long userId = 1L;
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(2L)
                .investmentTypeName("중립형")
                .investmentTypeDesc("적당한 위험을 감수하며 안정적인 수익을 추구합니다.")
                .build();

        when(typeTestService.getTestResultByUserId(userId)).thenReturn(expectedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.getTestResultByUserId(userId);

        // Then
        assertNotNull(actualResult);
        assertEquals(Long.valueOf(2L), actualResult.getInvestmentTypeId());
        assertEquals("중립형", actualResult.getInvestmentTypeName());
    }

    @Test
    @DisplayName("유형검사 결과 조회 - 결과가 없을 때 null이 반환되는지 테스트")
    void 유형검사_결과가_없을_때_null이_반환되는지() {
        // Given
        Long userId = 999L; // 존재하지 않는 사용자 ID

        when(typeTestService.getTestResultByUserId(userId)).thenReturn(null);

        // When
        TypeTestResultDTO actualResult = typeTestService.getTestResultByUserId(userId);

        // Then
        assertNull(actualResult);
    }

    @Test
    @DisplayName("유형검사 결과 업데이트 - 기존 결과가 새로운 결과로 업데이트되는지 테스트")
    void 기존_유형검사_결과가_새로운_결과로_업데이트되는지() {
        // Given
        Long memberId = 1L;
        Map<String, Object> newAnswers = new HashMap<>();
        newAnswers.put("answers", Arrays.asList(3, 3, 2, 3, 3)); // 더 공격적인 답변
        
        TypeTestResultDTO updatedResult = TypeTestResultDTO.builder()
                .investmentTypeId(3L)
                .investmentTypeName("공격형")
                .investmentTypeDesc("높은 수익을 위해 높은 위험도 감수할 수 있습니다.")
                .build();

        when(typeTestService.submitAnswersWithMemberId(eq(newAnswers), eq(memberId)))
                .thenReturn(updatedResult);

        // When
        TypeTestResultDTO actualResult = typeTestService.submitAnswersWithMemberId(newAnswers, memberId);

        // Then
        assertEquals(Long.valueOf(3L), actualResult.getInvestmentTypeId());
        assertEquals("공격형", actualResult.getInvestmentTypeName());
    }

    private Question createQuestion(Long questionId, String questionText) {
        Question question = new Question();
        question.setId(questionId);
        question.setQuestionText(questionText);
        return question;
    }
}

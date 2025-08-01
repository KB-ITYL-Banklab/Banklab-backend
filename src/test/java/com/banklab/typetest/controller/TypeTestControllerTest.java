package com.banklab.typetest.controller;

import com.banklab.security.service.LoginUserProvider;
import com.banklab.typetest.domain.Question;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.service.TypeTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("유형검사 컨트롤러 테스트")
class TypeTestControllerTest {

    @Mock
    private TypeTestService typeTestService;

    private TypeTestController typeTestController;

    @Mock
    private LoginUserProvider loginUserProvider;

    @BeforeEach
    void setUp() {
        typeTestController = new TypeTestController(typeTestService, loginUserProvider);
    }

    @Test
    @DisplayName("유형검사 질문 조회시 질문이 잘 나오는지")
    void 유형검사_질문_조회시_질문이_잘_나오는지() {
        // Given
        Question question1 = new Question();
        question1.setId(1L);
        question1.setQuestionText("투자 경험이 어느 정도이신가요?");

        Question question2 = new Question();
        question2.setId(2L);
        question2.setQuestionText("투자 손실을 어느 정도 감수할 수 있나요?");

        List<Question> questions = Arrays.asList(question1, question2);

        when(typeTestService.getAllQuestions()).thenReturn(questions);

        // When
        ResponseEntity<?> response = typeTestController.getAllQuestions();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(typeTestService).getAllQuestions();
    }

    @Test
    @DisplayName("사용자가 유형검사를 제출했을 때 OK 메시지가 잘 뜨는지")
    void 사용자가_유형검사를_제출했을_때_OK_메시지가_잘_뜨는지() {
        // Given
        Long memberId = 1L;

        Map<String, Object> payload = new HashMap<>();
        payload.put("answers", Arrays.asList(1, 2, 3, 1, 2));

        when(loginUserProvider.getLoginMemberId()).thenReturn(memberId);
        // When
        ResponseEntity<Map<String, String>> response = typeTestController.submitAnswers(payload);

        // Then
         assertEquals(200, response.getStatusCodeValue());
         assertNotNull(response.getBody());
         assertEquals("OK", response.getBody().get("message"));

         verify(typeTestService).submitAnswersWithMemberId(any(Map.class), eq(memberId));
    }
        @Test
        @DisplayName("유형검사 결과 조회를 했을 때 앞서 테스트한 결과가 잘 뜨는지")
        void 유형검사_결과_조회를_했을_때_앞서_테스트한_결과가_잘_뜨는지() {
        // Given
        Long memberId = 1L;
        TypeTestResultDTO expectedResult = TypeTestResultDTO.builder()
                .investmentTypeId(2L)
                .investmentTypeName("중립형")
                .investmentTypeDesc("적당한 위험을 감수하며 안정적인 수익을 추구합니다.")
                .build();

        when(loginUserProvider.getLoginMemberId()).thenReturn(memberId);
        when(typeTestService.getTestResultByUserId(memberId)).thenReturn(expectedResult);

        // When
        ResponseEntity<TypeTestResultDTO> response = typeTestController.getTestResult();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(Long.valueOf(2L), response.getBody().getInvestmentTypeId());
        assertEquals("중립형", response.getBody().getInvestmentTypeName());

        verify(loginUserProvider).getLoginMemberId();
        verify(typeTestService).getTestResultByUserId(memberId);
    }

    @Test
    @DisplayName("유형검사 결과가 존재하지 않으면 유형검사를 하라고 메시지가 뜨는지")
    void 유형검사_결과가_존재하지_않으면_유형검사를_하라고_메시지가_뜨는지() {
        // Given
        Long memberId = 1L;
        when(loginUserProvider.getLoginMemberId()).thenReturn(memberId);
        when(typeTestService.getTestResultByUserId(memberId)).thenReturn(null);

        // When
        ResponseEntity<TypeTestResultDTO> response = typeTestController.getTestResult();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("검사 결과가 없습니다. 먼저 검사를 진행하세요.", response.getBody().getMessage());

        verify(loginUserProvider).getLoginMemberId();
        verify(typeTestService).getTestResultByUserId(memberId);
    }

    @Test
    @DisplayName("investmentTypeId가 null인 경우 메시지가 뜨는지")
    void investmentTypeId가_null인_경우_메시지가_뜨는지() {
        // Given
        Long memberId = 1L;
        TypeTestResultDTO resultWithoutType = TypeTestResultDTO.builder()
                .investmentTypeId(null)
                .message("검사가 완료되지 않았습니다.")
                .build();
        when(loginUserProvider.getLoginMemberId()).thenReturn(memberId);
        when(typeTestService.getTestResultByUserId(memberId)).thenReturn(resultWithoutType);

        // When
        ResponseEntity<TypeTestResultDTO> response = typeTestController.getTestResult();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("검사 결과가 없습니다. 먼저 검사를 진행하세요.", response.getBody().getMessage());

        verify(loginUserProvider).getLoginMemberId();
        verify(typeTestService).getTestResultByUserId(memberId);
    }

    @Test
    @DisplayName("memberId가 null일 때 처리 테스트")
    void memberId가_null일_때_처리_테스트() {
        // Given
        when(loginUserProvider.getLoginMemberId()).thenReturn(null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("answers", Arrays.asList(1, 2, 3));
        // When
        ResponseEntity<Map<String, String>> response = typeTestController.submitAnswers(payload);
        // Then
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("유효하지 않은 사용자입니다.", response.getBody().get("message"));
        verify(typeTestService, never()).submitAnswersWithMemberId(any(), any());
    }
}

package com.banklab.typetest.controller;

import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.service.TypeTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypeTestController 테스트")
class TypeTestControllerTest {

    @Mock
    private TypeTestService typeTestService;

    private TypeTestController controller;

    @BeforeEach
    void setUp() {
        controller = new TypeTestController(typeTestService);
    }

    @Test
    @DisplayName("유효한 답변 제출 시 성공 응답을 반환한다")
    void 유효한_답변_제출_시_성공_응답_반환_테스트() {
        // Given
        Map<String, Object> payload = createValidPayload();
        TypeTestResultDTO expectedResult = createSuccessResult();
        when(typeTestService.submitAnswers(payload)).thenReturn(expectedResult);

        // When
        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(payload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResult);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getInvestmentTypeName()).isEqualTo("공격형");
    }

    @Test
    @DisplayName("잘못된 답변 데이터 제출 시 실패 응답을 반환한다")
    void 잘못된_답변_제출_시_실패_응답_반환_테스트() {
        // Given
        Map<String, Object> payload = createValidPayload();
        TypeTestResultDTO expectedResult = createFailResult();
        when(typeTestService.submitAnswers(payload)).thenReturn(expectedResult);

        // When
        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(payload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResult);
        assertThat(response.getBody().getMessage()).contains("점수 계산 결과가 없습니다");
    }

    private Map<String, Object> createValidPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        payload.put("answers", Collections.emptyList());
        return payload;
    }

    private TypeTestResultDTO createSuccessResult() {
        return TypeTestResultDTO.builder()
                .userId(1L)
                .investmentTypeId(2L)
                .investmentTypeName("공격형")
                .investmentTypeDesc("공격적으로 투자하는 유형")
                .message("ok")
                .build();
    }

    private TypeTestResultDTO createFailResult() {
        return TypeTestResultDTO.fail("점수 계산 결과가 없습니다. 답변 데이터를 확인하세요.");
    }
}

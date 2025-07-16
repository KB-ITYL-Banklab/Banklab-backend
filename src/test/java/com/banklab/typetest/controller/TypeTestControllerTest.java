package com.banklab.typetest.controller;

import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.service.TypeTestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeTestControllerTest {

    @Test
    void submitAnswers_returnsResponseEntity() {
        TypeTestService service = Mockito.mock(TypeTestService.class);
        TypeTestController controller = new TypeTestController(service);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        payload.put("answers", new Object[]{});

        TypeTestResultDTO dto = new TypeTestResultDTO(1L, 2L, "공격형", "공격적으로 투자하는 유형", "ok");
        Mockito.when(service.submitAnswers(payload)).thenReturn(dto);

        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(payload);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void submitAnswers_returnsFailResponseEntity() {
        TypeTestService service = Mockito.mock(TypeTestService.class);
        TypeTestController controller = new TypeTestController(service);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        payload.put("answers", new Object[]{});

        TypeTestResultDTO dto = TypeTestResultDTO.fail("점수 계산 결과가 없습니다. 답변 데이터를 확인하세요.");
        Mockito.when(service.submitAnswers(payload)).thenReturn(dto);

        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(payload);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
        assertTrue(response.getBody().getMessage().contains("점수 계산 결과가 없습니다"));
    }
}

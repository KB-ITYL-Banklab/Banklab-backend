package com.banklab.typetest.controller;

import com.banklab.typetest.domain.Question;
import com.banklab.product.domain.ProductType;
import com.banklab.risk.domain.RiskLevel;
import com.banklab.security.util.JwtProcessor;
import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.service.TypeTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("TypeTestController 테스트")
class TypeTestControllerTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private JwtProcessor jwtProcessor;

    @Mock
    private TypeTestService typeTestService;

    @InjectMocks
    private TypeTestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    //유형 검사 질문 조회 테스트
    @Test
    @DisplayName("유형검사 질문 조회")
    void 유형검사_질문_조회_API_테스트() {
        // Given
        List<Question> mockQuestions = List.of(createQuestion(1L, "질문1", "A", "B"));
        when(typeTestService.getAllQuestions()).thenReturn(mockQuestions);
        // When
        ResponseEntity<List<Question>> response = controller.getAllQuestions();
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getQuestionText()).isEqualTo("질문1");
    }

    //유형검사 제출 테스트
    @Test
    @DisplayName("유형검사 제출")
    void 유형검사_제출_API_테스트() {
        // Given
        Map<String, Object> payload = createValidPayload();
        TypeTestResultDTO expectedResult = createSuccessResult();
        String token = "test-token";
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProcessor.getMemberId(token)).thenReturn(1L);
        when(typeTestService.submitAnswersWithMemberId(payload, 1L)).thenReturn(expectedResult);

        // When
        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(mockRequest, payload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getInvestmentTypeName()).isEqualTo("공격형");
    }

    @Test
    @DisplayName("잘못된 답변 데이터 제출 시 실패 응답을 반환한다")
    void 잘못된_답변_제출_시_실패_응답_반환_테스트() {
        // Given
        Map<String, Object> payload = createValidPayload();
        TypeTestResultDTO expectedResult = createFailResult();
        String token = "test-token";
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProcessor.getMemberId(token)).thenReturn(1L);
        when(typeTestService.submitAnswersWithMemberId(payload, 1L)).thenReturn(expectedResult);

        // When
        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(mockRequest, payload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResult);
        assertThat(response.getBody().getMessage()).contains("점수 계산 결과가 없습니다");
    }

    @Test
    @DisplayName("유형검사 결과 및 추천상품 조회")
    void 유형검사_결과_및_추천상품_조회_컨트롤러_테스트() {
        // Given
        TypeTestResultDTO resultDTO = createSuccessResult();
        String token = "test-token";
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProcessor.getMemberId(token)).thenReturn(1L);
        when(typeTestService.getTestResultByUserId(1L)).thenReturn(resultDTO);

        // When
        ResponseEntity<TypeTestResultDTO> response = controller.getTestResultByToken(mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getRecommendedProducts()).isNotNull();
    }

    @Test
    @DisplayName("유형검사 답변 제출")
    void 유형검사_답변_제출_테스트() {
        String token = "test-token";
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProcessor.getMemberId(token)).thenReturn(1L);

        // 사용자가 선택한 답변 리스트
        List<Map<String, Object>> answers = List.of(
            Map.of("question_id", 1, "choice", "A"),
            Map.of("question_id", 2, "choice", "B"),
            Map.of("question_id", 3, "choice", "A"),
            Map.of("question_id", 4, "choice", "B"),
            Map.of("question_id", 5, "choice", "A"),
            Map.of("question_id", 6, "choice", "B"),
            Map.of("question_id", 7, "choice", "A"),
            Map.of("question_id", 8, "choice", "B"),
            Map.of("question_id", 9, "choice", "A"),
            Map.of("question_id", 10, "choice", "B")
        );
        Map<String, Object> payload = new HashMap<>();
        payload.put("answers", answers);

        TypeTestResultDTO resultDTO = TypeTestResultDTO.builder().userId(1L).message("success").build();
        when(typeTestService.submitAnswersWithMemberId(payload, 1L)).thenReturn(resultDTO);

        ResponseEntity<TypeTestResultDTO> response = controller.submitAnswers(mockRequest, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getMessage()).isEqualTo("success");
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
                .recommendedProducts(List.of(createDepositProduct()))
                .build();
    }
    private RecommendedProductDTO createDepositProduct() {
        return RecommendedProductDTO.builder()
                .productId(47L)
                .productType(ProductType.valueOf("DEPOSIT"))
                .productName("LIVE정기예금")
                .companyName("부산은행")
                .riskLevel(RiskLevel.valueOf("LOW"))
                .riskReason("LIVE정기예금은 우대 이율 조건이 비교적 단순하며, 가입금액 및 기간 제한도 없어 위험 요소가 낮은 편이므로 저위험(LOW) 수준으로 평가됩니다.")
                .interestRate("2.5~3.5%")
                .productFeature("안전한 예금상품")
                .targetCustomer("안정적인 수익을 원하는 고객")
                .build();
    }

    private TypeTestResultDTO createFailResult() {
        return TypeTestResultDTO.fail("점수 계산 결과가 없습니다. 답변 데이터를 확인하세요.");
    }

    private Question createQuestion(Long id, String questionText, String choiceAText, String choiceBText) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText(questionText);
        q.setChoiceAText(choiceAText);
        q.setChoiceBText(choiceBText);
        return q;
    }
}

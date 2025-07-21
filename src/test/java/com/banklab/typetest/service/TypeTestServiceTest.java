package com.banklab.typetest.service;

import com.banklab.typetest.domain.ChoiceType;
import com.banklab.typetest.domain.InvestmentType;
import com.banklab.typetest.domain.QuestionChoiceScore;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.mapper.InvestmentTypeMapper;
import com.banklab.typetest.mapper.QuestionChoiceScoreMapper;
import com.banklab.typetest.mapper.QuestionMapper;
import com.banklab.typetest.mapper.UserInvestmentTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypeTestService 테스트")
class TypeTestServiceTest {

    @Mock
    private QuestionMapper questionMapper;
    
    @Mock
    private QuestionChoiceScoreMapper scoreMapper;
    
    @Mock
    private InvestmentTypeMapper investmentTypeMapper;
    
    @Mock
    private UserInvestmentTypeMapper userInvestmentTypeMapper;

    private TypeTestService service;

    @BeforeEach
    void setUp() {
        service = new TypeTestServiceImpl(
                questionMapper, scoreMapper, investmentTypeMapper, userInvestmentTypeMapper);
    }

    @Test
    @DisplayName("유효한 답변 제출 시 올바른 투자유형을 반환한다")
    void 유효한_답변을_제출_시_올바른_투자유형을_반환_테스트() {
        // Given: 유효한 페이로드와 Mock 데이터 설정
        Map<String, Object> payload = createValidPayload();
        QuestionChoiceScore score = createQuestionChoiceScore();
        InvestmentType investmentType = createInvestmentType();
        
        when(scoreMapper.findByQuestionIdAndChoice(1L, ChoiceType.A)).thenReturn(score);
        when(investmentTypeMapper.findById(2L)).thenReturn(investmentType);
        when(userInvestmentTypeMapper.findByUserId(1L)).thenReturn(null);

        // When: 답변 제출
        TypeTestResultDTO result = service.submitAnswers(payload);

        // Then: 결과 검증
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getInvestmentTypeId()).isEqualTo(2L);
        assertThat(result.getInvestmentTypeName()).isEqualTo("공격형");
        assertThat(result.getInvestmentTypeDesc()).isEqualTo("공격적으로 투자하는 유형");
        assertThat(result.getMessage()).isEqualTo("투자유형 계산이 완료되었습니다.");
    }

    @Test
    @DisplayName("점수 계산 결과가 없을 때 실패 결과를 반환한다")
    void 점수_계산_결과가_없으면_실패_결과를_반환한다() {
        // Given: 점수 계산 결과가 없는 페이로드 설정
        Map<String, Object> payload = createValidPayload();
        when(scoreMapper.findByQuestionIdAndChoice(1L, ChoiceType.A)).thenReturn(null);

        // When: 답변 제출
        TypeTestResultDTO result = service.submitAnswers(payload);

        // Then: 실패 메시지 검증
        assertThat(result.getInvestmentTypeId()).isNull();
        assertThat(result.getMessage()).contains("점수 계산 결과가 없습니다");
    }

    @Test
    @DisplayName("잘못된 답변 형식일 때 에러 메시지를 반환한다")
    void 잘못된_답변_형식_제출_시_에러_메시지를_반환_테스트() {
        // Given: 잘못된 페이로드 설정
        Map<String, Object> payload = createInvalidPayload();

        // When: 답변 제출
        TypeTestResultDTO result = service.submitAnswers(payload);

        // Then: 에러 메시지 검증
        assertThat(result.getMessage()).contains("answers 필드가 올바르지 않습니다");
    }

    private Map<String, Object> createValidPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        
        List<Map<String, Object>> answers = new ArrayList<>();
        Map<String, Object> answer = new HashMap<>();
        answer.put("question_id", 1L);
        answer.put("choice", "A");
        answers.add(answer);
        
        payload.put("answers", answers);
        return payload;
    }

    private Map<String, Object> createInvalidPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        payload.put("answers", "잘못된타입");
        return payload;
    }

    private QuestionChoiceScore createQuestionChoiceScore() {
        QuestionChoiceScore score = new QuestionChoiceScore();
        score.setId(1L);
        score.setQuestionId(1L);
        score.setChoice(ChoiceType.A);
        score.setInvestmentTypeId(2L);
        score.setScore(5);
        return score;
    }

    private InvestmentType createInvestmentType() {
        InvestmentType investmentType = new InvestmentType();
        investmentType.setId(2L);
        investmentType.setInvestmentTypeName("공격형");
        investmentType.setInvestmentTypeDesc("공격적으로 투자하는 유형");
        return investmentType;
    }
}

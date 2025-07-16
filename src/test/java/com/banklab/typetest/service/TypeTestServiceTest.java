package com.banklab.typetest.service;

import com.banklab.typetest.domain.ChoiceType;
import com.banklab.typetest.domain.QuestionChoiceScore;
import com.banklab.typetest.domain.InvestmentType;
import com.banklab.typetest.dto.AnswerDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.mapper.QuestionMapper;
import com.banklab.typetest.mapper.QuestionChoiceScoreMapper;
import com.banklab.typetest.mapper.InvestmentTypeMapper;
import com.banklab.typetest.mapper.UserInvestmentTypeMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TypeTestServiceTest {
    QuestionMapper questionMapper = Mockito.mock(QuestionMapper.class);
    QuestionChoiceScoreMapper scoreMapper = Mockito.mock(QuestionChoiceScoreMapper.class);
    InvestmentTypeMapper investmentTypeMapper = Mockito.mock(InvestmentTypeMapper.class);
    UserInvestmentTypeMapper userInvestmentTypeMapper = Mockito.mock(UserInvestmentTypeMapper.class);

    @Test
    void submitAnswers_returnsCorrectInvestmentType() {

        TypeTestService service = new TypeTestServiceImpl(
                questionMapper, scoreMapper, investmentTypeMapper, userInvestmentTypeMapper);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        List<Map<String, Object>> answers = new ArrayList<>();
        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("question_id", 1L);
        answer1.put("choice", "A");
        answers.add(answer1);
        payload.put("answers", answers);

        QuestionChoiceScore score = new QuestionChoiceScore();
        score.setId(1L);
        score.setQuestionId(1L);
        score.setChoice(ChoiceType.A);
        score.setInvestmentTypeId(2L);
        score.setScore(5);

        Mockito.when(scoreMapper.findByQuestionIdAndChoice(1L, ChoiceType.A))
                .thenReturn(score);

        InvestmentType investmentType = new InvestmentType();
        investmentType.setId(2L);
        investmentType.setInvestmentTypeName("공격형");
        investmentType.setInvestmentTypeDesc("공격적으로 투자하는 유형");

        Mockito.when(investmentTypeMapper.findById(2L))
                .thenReturn(investmentType);

        Mockito.when(userInvestmentTypeMapper.findByUserId(1L)).thenReturn(null);

        TypeTestResultDTO result = service.submitAnswers(payload);

        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getInvestmentTypeId());
        assertEquals("공격형", result.getInvestmentTypeName());
        assertEquals("공격적으로 투자하는 유형", result.getInvestmentTypeDesc());
        assertEquals("투자유형 계산이 완료되었습니다.", result.getMessage());
    }

    @Test
    void submitAnswers_returnsFailIfNoScore() {
        // 점수 계산 결과가 없을 때
        QuestionMapper questionMapper = Mockito.mock(QuestionMapper.class);
        QuestionChoiceScoreMapper scoreMapper = Mockito.mock(QuestionChoiceScoreMapper.class);
        InvestmentTypeMapper investmentTypeMapper = Mockito.mock(InvestmentTypeMapper.class);
        UserInvestmentTypeMapper userInvestmentTypeMapper = Mockito.mock(UserInvestmentTypeMapper.class);

        TypeTestService service = new TypeTestServiceImpl(
                questionMapper, scoreMapper, investmentTypeMapper, userInvestmentTypeMapper);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        List<Map<String, Object>> answers = new ArrayList<>();
        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("question_id", 1L);
        answer1.put("choice", "A");
        answers.add(answer1);
        payload.put("answers", answers);

        Mockito.when(scoreMapper.findByQuestionIdAndChoice(1L, ChoiceType.A)).thenReturn(null);

        TypeTestResultDTO result = service.submitAnswers(payload);

        assertNull(result.getInvestmentTypeId());
        assertTrue(result.getMessage().contains("점수 계산 결과가 없습니다"));
    }

    @Test
    void submitAnswers_throwsExceptionOnInvalidPayload() {
        QuestionMapper questionMapper = Mockito.mock(QuestionMapper.class);
        QuestionChoiceScoreMapper scoreMapper = Mockito.mock(QuestionChoiceScoreMapper.class);
        InvestmentTypeMapper investmentTypeMapper = Mockito.mock(InvestmentTypeMapper.class);
        UserInvestmentTypeMapper userInvestmentTypeMapper = Mockito.mock(UserInvestmentTypeMapper.class);

        TypeTestService service = new TypeTestServiceImpl(
                questionMapper, scoreMapper, investmentTypeMapper, userInvestmentTypeMapper);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", 1L);
        // answers 필드가 잘못된 타입
        payload.put("answers", "잘못된타입");

        TypeTestResultDTO result = service.submitAnswers(payload);

        assertTrue(result.getMessage().contains("answers 필드가 올바르지 않습니다"));
    }
}

package com.banklab.typetest.service;

import com.banklab.typetest.domain.ChoiceType;
import com.banklab.typetest.domain.InvestmentType;
import com.banklab.typetest.domain.Question;
import com.banklab.typetest.domain.QuestionChoiceScore;
import com.banklab.typetest.domain.UserInvestmentType;
import com.banklab.typetest.dto.AnswerDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.mapper.InvestmentTypeMapper;
import com.banklab.typetest.mapper.QuestionChoiceScoreMapper;
import com.banklab.typetest.mapper.QuestionMapper;
import com.banklab.typetest.mapper.UserInvestmentTypeMapper;
import com.banklab.typetest.util.TypeTestMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeTestServiceImpl implements TypeTestService {

    private final QuestionMapper questionMapper;
    private final QuestionChoiceScoreMapper questionChoiceScoreMapper;
    private final InvestmentTypeMapper investmentTypeMapper;
    private final UserInvestmentTypeMapper userInvestmentTypeMapper;

    @Override
    public List<Question> getAllQuestions() {
        return questionMapper.getAllQuestions();
    }

    @Override
    public TypeTestResultDTO submitAnswers(Map<String, Object> payload) {
        try {
            //사용자 ID와 답변 데이터 추출
            Long userId = extractUserId(payload);
            List<AnswerDTO> answers = parseAnswers(payload);

            //답변을 기반으로 투자 성향 계산
            Long bestTypeId = calculateBestInvestmentType(answers);
            if (bestTypeId == null) {
                return createFailResult(TypeTestMessageUtil.FAIL_SCORE_MSG);
            }

            // 투자 성향 정보 조회 및 저장
            InvestmentType investmentType = getInvestmentType(bestTypeId);
            saveUserInvestmentType(userId, bestTypeId);

            //성공 결과 반환
            return createSuccessResult(userId, investmentType);

        } catch (IllegalArgumentException e) {
            log.warn("투자성향 테스트 실행 중 잘못된 파라미터: {}", e.getMessage());
            return createFailResult(e.getMessage());
        } catch (Exception e) {
            log.error("투자성향 테스트 실행 중 예상치 못한 오류 발생", e);
            return createFailResult(TypeTestMessageUtil.SERVER_ERROR_MSG + e.getMessage());
        }
    }

    private Long extractUserId(Map<String, Object> payload) {
        Object userIdObj = payload.get("user_id");
        if (userIdObj == null) {
            throw new IllegalArgumentException("user_id 값이 누락되었습니다.");
        }
        return Long.valueOf(userIdObj.toString());
    }

    private List<AnswerDTO> parseAnswers(Map<String, Object> payload) {
        Object rawAnswers = payload.get("answers");
        if (!(rawAnswers instanceof List<?> answersRaw)) {
            throw new IllegalArgumentException("answers 필드가 올바르지 않습니다.");
        }
        return answersRaw.stream()
                .map(this::parseAnswer)
                .toList();
    }

    private AnswerDTO parseAnswer(Object answerObj) {
        // 개별 답변 파싱
        if (!(answerObj instanceof Map<?, ?> answerMap)) {
            throw new IllegalArgumentException("answers 배열의 요소가 올바르지 않습니다.");
        }

        Object questionId = answerMap.get("question_id");
        Object choice = answerMap.get("choice");

        if (questionId == null || choice == null) {
            throw new IllegalArgumentException("question_id 또는 choice 값이 누락되었습니다.");
        }

        try {
            return AnswerDTO.builder()
                    .questionId(Long.valueOf(questionId.toString()))
                    .choice(ChoiceType.valueOf(choice.toString()))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("답변 데이터 변환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Long calculateBestInvestmentType(List<AnswerDTO> answers) {
        // 답변을 기반으로 투자 성향 점수 계산
        Map<Long, Integer> typeScores = new HashMap<>();

        for (AnswerDTO answer : answers) {
            addScoreForAnswer(answer, typeScores);
        }

        return findTypeWithHighestScore(typeScores);
    }

    private void addScoreForAnswer(AnswerDTO answer, Map<Long, Integer> typeScores) {
        // 답변에 해당하는 점수를 투자 성향 점수에 추가
        QuestionChoiceScore score = questionChoiceScoreMapper
                .findByQuestionIdAndChoice(answer.getQuestionId(), answer.getChoice());
        
        if (score != null) {
            typeScores.merge(score.getInvestmentTypeId(), score.getScore(), Integer::sum);
        }
    }

    private Long findTypeWithHighestScore(Map<Long, Integer> typeScores) {
        // 가장 높은 점수를 가진 투자 성향 ID 반환
        return typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private InvestmentType getInvestmentType(Long typeId) {
        // 투자 성향 정보 조회
        return Optional.ofNullable(investmentTypeMapper.findById(typeId))
                .orElse(createDefaultInvestmentType());
    }

    private InvestmentType createDefaultInvestmentType() {
        // 기본 투자 성향 생성
        InvestmentType defaultType = new InvestmentType();
        defaultType.setInvestmentTypeName("알 수 없음");
        defaultType.setInvestmentTypeDesc("투자성향을 확인할 수 없습니다.");
        return defaultType;
    }

    private void saveUserInvestmentType(Long userId, Long investmentTypeId) {
        // 사용자 투자 성향 저장
        UserInvestmentType userType = Optional.ofNullable(userInvestmentTypeMapper.findByUserId(userId))
                .orElse(new UserInvestmentType());

        userType.setUserId(userId);
        userType.setInvestmentTypeId(investmentTypeId);
        userType.setEvaluationDate(LocalDate.now());

        if (userType.getId() == null) {
            userInvestmentTypeMapper.insertUserInvestmentType(userType);
        } else {
            userInvestmentTypeMapper.updateUserInvestmentType(userType);
        }
    }

    private TypeTestResultDTO createSuccessResult(Long userId, InvestmentType investmentType) {
        // 성공 결과 생성
        return TypeTestResultDTO.builder()
                .userId(userId)
                .investmentTypeId(investmentType.getId())
                .investmentTypeName(investmentType.getInvestmentTypeName())
                .investmentTypeDesc(investmentType.getInvestmentTypeDesc())
                .message(TypeTestMessageUtil.SUCCESS_MSG)
                .build();
    }

    private TypeTestResultDTO createFailResult(String message) {
        // 실패 결과 생성
        return TypeTestResultDTO.builder()
                .message(message)
                .build();
    }
}

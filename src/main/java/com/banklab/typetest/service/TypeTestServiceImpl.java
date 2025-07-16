package com.banklab.typetest.service;

import com.banklab.typetest.domain.Question;
import com.banklab.typetest.domain.QuestionChoiceScore;
import com.banklab.typetest.domain.InvestmentType;
import com.banklab.typetest.domain.UserInvestmentType;
import com.banklab.typetest.dto.AnswerDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.mapper.QuestionMapper;
import com.banklab.typetest.mapper.QuestionChoiceScoreMapper;
import com.banklab.typetest.mapper.InvestmentTypeMapper;
import com.banklab.typetest.mapper.UserInvestmentTypeMapper;
import com.banklab.typetest.util.TypeTestMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            Long userId = extractUserId(payload);
            List<AnswerDTO> answers = parseAnswers(payload);

            Long bestTypeId = findBestInvestmentTypeId(answers);
            if (bestTypeId == null) {
                return TypeTestResultDTO.builder()
                        .message(TypeTestMessageUtil.FAIL_SCORE_MSG)
                        .build();
            }

            InvestmentType investmentType = Optional.ofNullable(investmentTypeMapper.findById(bestTypeId)).orElse(new InvestmentType());
            upsertUserInvestmentType(userId, bestTypeId);

            return TypeTestResultDTO.builder()
                    .userId(userId)
                    .investmentTypeId(bestTypeId)
                    .investmentTypeName(Optional.ofNullable(investmentType.getInvestmentTypeName()).orElse("N/A"))
                    .investmentTypeDesc(Optional.ofNullable(investmentType.getInvestmentTypeDesc()).orElse("N/A"))
                    .message(TypeTestMessageUtil.SUCCESS_MSG)
                    .build();
        } catch (Exception e) {
            return TypeTestResultDTO.builder()
                    .message(TypeTestMessageUtil.SERVER_ERROR_MSG + e.getMessage())
                    .build();
        }
    }

    private Long extractUserId(Map<String, Object> payload) {
        Object userIdObj = payload.get("user_id");
        if (userIdObj == null) throw new IllegalArgumentException("user_id 값이 누락되었습니다.");
        return Long.valueOf(userIdObj.toString());
    }

    private List<AnswerDTO> parseAnswers(Map<String, Object> payload) {
        Object rawAnswers = payload.get("answers");
        if (!(rawAnswers instanceof List<?> answersRaw)) {
            throw new IllegalArgumentException("answers 필드가 올바르지 않습니다.");
        }
        try {
            return answersRaw.stream().map(a -> {
                if (!(a instanceof Map<?,?> answerMap)) {
                    throw new IllegalArgumentException("answers 배열의 요소가 올바르지 않습니다.");
                }
                Object qid = answerMap.get("question_id");
                Object choice = answerMap.get("choice");
                if (qid == null || choice == null) {
                    throw new IllegalArgumentException("question_id 또는 choice 값이 누락되었습니다.");
                }
                return AnswerDTO.builder()
                        .questionId(Long.valueOf(qid.toString()))
                        .choice(Enum.valueOf(com.banklab.typetest.domain.ChoiceType.class, choice.toString()))
                        .build();
            }).toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("answers 파싱 중 오류: " + e.getMessage(), e);
        }
    }

    private Long findBestInvestmentTypeId(List<AnswerDTO> answers) {
        Map<Long, Integer> scoreMap = new HashMap<>();
        answers.forEach(answer -> {
            QuestionChoiceScore score = questionChoiceScoreMapper.findByQuestionIdAndChoice(answer.getQuestionId(), answer.getChoice());
            if (score != null) {
                scoreMap.merge(score.getInvestmentTypeId(), score.getScore(), Integer::sum);
            }
        });
        return scoreMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void upsertUserInvestmentType(Long userId, Long bestTypeId) {
        UserInvestmentType userInvestmentType = Optional.ofNullable(userInvestmentTypeMapper.findByUserId(userId))
                .orElseGet(UserInvestmentType::new);
        userInvestmentType.setUserId(userId);
        userInvestmentType.setInvestmentTypeId(bestTypeId);
        userInvestmentType.setEvaluationDate(LocalDate.now());
        if (userInvestmentType.getId() == null) {
            userInvestmentTypeMapper.insertUserInvestmentType(userInvestmentType);
        } else {
            userInvestmentTypeMapper.updateUserInvestmentType(userInvestmentType);
        }
    }
}

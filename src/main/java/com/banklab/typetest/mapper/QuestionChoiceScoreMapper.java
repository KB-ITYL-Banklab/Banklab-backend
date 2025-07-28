package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.enums.ChoiceType;
import com.banklab.typetest.domain.QuestionChoiceScore;
import org.apache.ibatis.annotations.Param;


public interface QuestionChoiceScoreMapper {
    /**
     * 질문 ID와 선택지를 기준으로 QuestionChoiceScore를 조회한다.
     * @param questionId 질문 ID
     * @param choice 질문 ID에 대한 선택지
     * @return
     */
    QuestionChoiceScore findByQuestionIdAndChoice(@Param("questionId") Long questionId, @Param("choice") ChoiceType choice);
}
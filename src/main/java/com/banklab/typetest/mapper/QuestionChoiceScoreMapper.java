package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.ChoiceType;
import com.banklab.typetest.domain.QuestionChoiceScore;
import org.apache.ibatis.annotations.Param;


public interface QuestionChoiceScoreMapper {
    QuestionChoiceScore findByQuestionIdAndChoice(@Param("questionId") Long questionId, @Param("choice") ChoiceType choice);
}

package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {
    List<Question> getAllQuestions();
    
    /**
     * 질문 ID로 개별 질문 조회 (새로 추가)
     */
    Question findById(@Param("id") Long id);
}

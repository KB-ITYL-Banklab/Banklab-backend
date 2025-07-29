package com.banklab.typetest.domain;

import com.banklab.typetest.domain.enums.QuestionType;
import lombok.Data;

@Data
public class Question {
    private Long id;
    private String questionText;
    private String choiceAText;
    private String choiceBText;
    private QuestionType questionType;  // 질문 유형
    private Boolean isBlocking;         // 제한 여부
}
package com.banklab.typetest.domain;

import lombok.Data;

@Data
public class QuestionChoiceScore {
    private Long id;
    private Long questionId;
    private ChoiceType choice;
    private Long investmentTypeId;
    private Integer score;
}
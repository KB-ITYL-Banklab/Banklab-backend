package com.banklab.typetest.domain;

import lombok.Data;

@Data
public class Question {
    private Long id;
    private String questionText;
    private String choiceAText;
    private String choiceBText;
}

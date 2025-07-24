package com.banklab.typetest.dto;

import com.banklab.typetest.domain.ChoiceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerDTO {
    private Long questionId;
    private ChoiceType choice;
}

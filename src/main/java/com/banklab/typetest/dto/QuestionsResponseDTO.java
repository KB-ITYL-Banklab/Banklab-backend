package com.banklab.typetest.dto;

import com.banklab.typetest.domain.Question;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionsResponseDTO {
    private List<Question> questions;
}

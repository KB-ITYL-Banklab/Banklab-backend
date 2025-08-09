package com.banklab.typetest.dto;

import com.banklab.typetest.domain.Question;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "투자성향 검사 질문 목록 응답")
public class QuestionsResponseDTO {
    @ApiModelProperty(value = "질문 목록")
    private List<Question> questions;
}

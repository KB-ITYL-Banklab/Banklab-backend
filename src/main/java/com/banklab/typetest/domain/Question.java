package com.banklab.typetest.domain;

import com.banklab.typetest.domain.enums.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "투자성향 검사 질문")
public class Question {
    @ApiModelProperty(value = "질문 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "질문 내용", example = "투자할 때 가장 중요하게 생각하는 것은?")
    private String questionText;

    @ApiModelProperty(value = "선택지 A", example = "안정성")
    private String choiceAText;

    @ApiModelProperty(value = "선택지 B", example = "수익성")
    private String choiceBText;

    @ApiModelProperty(value = "질문 유형")
    private QuestionType questionType;  // 질문 유형

    @ApiModelProperty(value = "제한 여부", example = "false")
    private Boolean isBlocking; // 제한 여부
}
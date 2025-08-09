package com.banklab.typetest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "투자성향 검사 결과 응답 객체")
public class TypeTestResultDTO {
    @ApiModelProperty(value = "사용자 ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "투자성향 타입 ID", example = "1")
    private Long investmentTypeId;

    @ApiModelProperty(value = "투자성향 타입명", example = "안정형")
    private String investmentTypeName;

    @ApiModelProperty(value = "투자성향 설명", example = "안정성을 중시하는 투자 성향")
    private String investmentTypeDesc;

    @ApiModelProperty(value = "누적 조회수", example = "10")
    private Integer cumulativeViews;

    @ApiModelProperty(value = "응답 메시지", example = "검사 결과가 조회되었습니다")
    private String message;

    @ApiModelProperty(value = "업데이트 일시", example = "2024-08-09")
    private String updatedAt;

    @ApiModelProperty(value = "추천 상품 목록")
    private List<RecommendedProductDTO> recommendedProducts;

    public static TypeTestResultDTO success(Long userId, Long investmentTypeId, String investmentTypeName, String investmentTypeDesc, String message) {
        return TypeTestResultDTO.builder()
                .userId(userId)
                .investmentTypeId(investmentTypeId)
                .investmentTypeName(investmentTypeName)
                .investmentTypeDesc(investmentTypeDesc)
                .message(message)
                .build();
    }

    public static TypeTestResultDTO fail(String message) {
        return TypeTestResultDTO.builder()
                .message(message)
                .build();
    }
}

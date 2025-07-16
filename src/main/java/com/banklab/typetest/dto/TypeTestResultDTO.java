package com.banklab.typetest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeTestResultDTO {
    private Long userId;
    private Long investmentTypeId;
    private String investmentTypeName;
    private String investmentTypeDesc;
    private String message;

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

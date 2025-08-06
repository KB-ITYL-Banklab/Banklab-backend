package com.banklab.typetest.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserInvestmentType {
    private Long id;
    private Long userId;
    private Long investmentTypeId; //투자 유형
    private Integer cumulativeViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

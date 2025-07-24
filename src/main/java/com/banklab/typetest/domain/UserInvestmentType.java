package com.banklab.typetest.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInvestmentType {
    private Long id;
    private Long userId;
    private Long investmentTypeId;
    private LocalDate evaluationDate;
}

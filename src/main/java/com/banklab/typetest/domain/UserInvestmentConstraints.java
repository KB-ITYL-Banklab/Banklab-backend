package com.banklab.typetest.domain;

import com.banklab.typetest.domain.enums.ConstraintType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvestmentConstraints {
    private Long id;
    private Long userId;
    private ConstraintType constraintType; //제약조건
    private Boolean isActive;
}

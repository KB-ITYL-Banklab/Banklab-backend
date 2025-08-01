package com.banklab.typetest.domain;

import lombok.Data;

@Data
public class InvestmentType {
    private Long id;
    private String investmentTypeName;
    private String investmentTypeDesc;
}

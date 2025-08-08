package com.banklab.peerCompare.dto;


import lombok.Data;

@Data
public class CategoryComparisonDTO {
    private Long categoryId;
    private String categoryName;
    private Double avgExpense;  // 또래 평균
}

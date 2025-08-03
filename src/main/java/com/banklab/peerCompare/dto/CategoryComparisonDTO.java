package com.banklab.peerCompare.dto;


import lombok.Data;

@Data
public class CategoryComparisonDTO {
    private Long categoryId;
    private String categoryName;
    private double avgExpense;  // 또래 평균

    private Integer userExpense;    // 사용자 지출
    private Integer diff;       // 차이
    private Double percentDiff; // 차이율 (%)


}

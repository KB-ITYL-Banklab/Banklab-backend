package com.banklab.category.dto;

import com.banklab.category.domain.CategoryVO;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CategoryExpenseDTO {
    private Long categoryId;
    private String categoryName;
    private long totalExpense;
    private Integer expenseDays;   // COUNT(DISTINCT date)

    private Double avgExpense;     // service에서 set
}


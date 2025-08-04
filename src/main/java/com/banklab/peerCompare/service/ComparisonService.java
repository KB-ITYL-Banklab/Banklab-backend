package com.banklab.peerCompare.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;

import java.util.Date;
import java.util.List;

public interface ComparisonService {
    List<CategoryComparisonDTO> getPeerCategoryCompare(
            Long memberId,
            String email,
            Date startDate,
            Date endDate);

    List<CategoryExpenseDTO> getMyCategoryCompare(Long memberId,Date startDate, Date endDate);
}

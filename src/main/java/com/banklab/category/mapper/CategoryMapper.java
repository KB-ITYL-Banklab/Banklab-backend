package com.banklab.category.mapper;

import com.banklab.category.domain.CategoryVO;

public interface CategoryMapper {
    CategoryVO getCategoryById(Long id);
    CategoryVO getCategoryByName(String name);
    void createCategory(CategoryVO vo);
    int updateCategory(CategoryVO vo);
}

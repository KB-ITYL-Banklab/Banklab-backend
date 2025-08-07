package com.banklab.category.mapper;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface CategoryMapper {
    CategoryVO getCategoryById(Long id);

    CategoryVO getCategoryByName(String name);

    List<CategoryVO> findAll();

    void createCategory(CategoryVO vo);

    int updateCategory(CategoryVO vo);
}

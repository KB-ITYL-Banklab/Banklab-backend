package com.banklab.category.service;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 
     * @param id 카테고리 아이디
     * @return  해당 카테고리 아이디를 가진 카테고리
     */
    public CategoryDTO getCategoryById(Long id) {
        return Optional.ofNullable(CategoryDTO.of(categoryMapper.getCategoryById(id)))
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * 
     * @param name 카테고리 이름
     * @return 해당 카테고리 이름을 가진 카테고리
     */
    public CategoryDTO getCategoryByName(String name) {
        return Optional.ofNullable(CategoryDTO.of(categoryMapper.getCategoryByName(name)))
                .orElseThrow(NoSuchElementException::new);
    }


    public CategoryDTO createCategory(String name) {
        try {
            return getCategoryByName(name);
        }catch (NoSuchElementException e){
            CategoryVO vo = CategoryVO.builder().name(name).build();

            categoryMapper.createCategory(vo);
            return getCategoryById(vo.getId());
        }
    }

    public CategoryDTO updateCategory(CategoryDTO dto) {
        CategoryDTO category = getCategoryByName(dto.getName());
        
        // 업데하려는 카테고리 이름이 없는 경우 null 반환
        if(category == null) {
            return null;
        }
        
        CategoryVO categoryVO = dto.toVO();
        categoryMapper.updateCategory(categoryVO);

        return getCategoryById(categoryVO.getId());
    }

}

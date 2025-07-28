package com.banklab.category.dto;

import com.banklab.category.domain.CategoryVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;

    public static CategoryDTO of(CategoryVO vo) {
        return vo == null ? null : CategoryDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .build();
    }

    public CategoryVO toVO() {
        return CategoryVO.builder()
                .id(id)
                .name(name)
                .build();
    }
}

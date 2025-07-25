package com.banklab.financeContents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 금융용어 DTO
 * SEIBRO API 응답을 매핑하는 DTO 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceTermsDto {
    
    private Integer id;
    private String title;
    private String definition;
    
    @Override
    public String toString() {
        return "FinanceTermsDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}

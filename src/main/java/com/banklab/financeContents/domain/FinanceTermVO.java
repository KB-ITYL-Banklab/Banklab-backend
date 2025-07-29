package com.banklab.financeContents.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 금융용어 도메인 객체
 * finance_terms 테이블과 매핑되는 VO 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceTermVO {
    
    private Long id;                    // 금융용어 ID
    private String title;               // 금융용어 제목 (용어명)
    private String definition;          // 금융용어 정의 (설명)
    
    // 검색 편의를 위한 getter 별칭 메서드들
    public String getTerm() {
        return this.title;
    }
    
    public String getDescription() {
        return this.definition;
    }
    
    @Override
    public String toString() {
        return "FinanceTermVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}

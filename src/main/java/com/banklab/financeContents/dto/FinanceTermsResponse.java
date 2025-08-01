package com.banklab.financeContents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 금융용어 API 응답 DTO
 * 클라이언트에게 전달할 응답 구조를 정의
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceTermsResponse {
    
    private boolean success;
    private String message;
    private FinanceTermsDto data;
    private String searchTerm;
    
    /**
     * 성공 응답 생성
     */
    public static FinanceTermsResponse success(String searchTerm, FinanceTermsDto data) {
        return FinanceTermsResponse.builder()
            .success(true)
            .message("금융용어 검색 성공")
            .data(data)
            .searchTerm(searchTerm)
            .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static FinanceTermsResponse failure(String searchTerm, String message) {
        return FinanceTermsResponse.builder()
            .success(false)
            .message(message)
            .data(null)
            .searchTerm(searchTerm)
            .build();
    }
    
    @Override
    public String toString() {
        return "FinanceTermsResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", searchTerm='" + searchTerm + '\'' +
                '}';
    }
}

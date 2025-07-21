package com.banklab.financeContents.dto;

import lombok.Data;

/**
 * 네이버 뉴스 API 응답 데이터 전송 객체 (DTO)
 * 네이버 API에서 받은 뉴스 항목을 담는 클래스
 */
@Data
public class NewsItemDto {
    // 뉴스 제목
    private String title;
    
    // 원본 링크 (네이버가 아닌 언론사 링크)
    private String originallink;
    
    // 네이버 뉴스 링크
    private String link;
    
    // 뉴스 요약 내용
    private String description;
    
    // 뉴스 발행 날짜
    private String pubDate;

    // Lombok @Data 어노테이션으로 Getter & Setter 자동 생성


}

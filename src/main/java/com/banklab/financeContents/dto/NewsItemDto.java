package com.banklab.financeContents.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 네이버 뉴스 API 응답 데이터 전송 객체 (DTO)
 * 네이버 API에서 받은 뉴스 항목을 담는 클래스
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "뉴스 아이템 정보")
public class NewsItemDto {
    // 뉴스 제목
    @ApiModelProperty(value = "뉴스 제목", example = "코스피 상승세 지속")
    private String title;

    // 원본 링크 (네이버가 아닌 언론사 링크)
    @ApiModelProperty(value = "원본 링크", example = "https://www.example.com/news")
    private String originallink;

    // 네이버 뉴스 링크
    @ApiModelProperty(value = "네이버 뉴스 링크", example = "https://news.naver.com/...")
    private String link;

    // 뉴스 요약 내용
    @ApiModelProperty(value = "뉴스 요약 내용", example = "코스피가 연일 상승세를 보이고 있다...")
    private String description;

    // 뉴스 발행 날짜
    @ApiModelProperty(value = "발행일시", example = "Thu, 09 Aug 2024 10:30:00 +0900")
    private String pubDate;
}

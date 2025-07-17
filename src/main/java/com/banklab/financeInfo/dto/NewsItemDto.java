package com.banklab.financeInfo.dto;

import lombok.Data;

@Data
public class NewsItemDto {
    private String title;
    private String originallink;
    private String link;
    private String description;
    private String pubDate;

    // Getter & Setter
    // toString (선택)
}

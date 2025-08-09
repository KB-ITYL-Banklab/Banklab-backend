package com.banklab.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentViewLogVO {
    private Long id;
    private Long memberId;
    private ContentType contentType;
    private String contentKey;
    private Date viewDate;
}

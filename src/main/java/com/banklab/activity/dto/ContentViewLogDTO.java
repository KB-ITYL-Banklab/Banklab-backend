package com.banklab.activity.dto;

import com.banklab.activity.domain.ContentType;
import com.banklab.activity.domain.ContentViewLogVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContentViewLogDTO {
    private ContentType contentType;
    private String contentKey;

    public ContentViewLogVO toVO(Long memberId) {
        return ContentViewLogVO.builder()
                .memberId(memberId)
                .contentType(contentType)
                .contentKey(contentKey)
                .build();
    }
}

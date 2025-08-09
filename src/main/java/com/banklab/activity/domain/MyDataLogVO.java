package com.banklab.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyDataLogVO {
    private Long id;
    private Long memberId;
    private String fetchType;
    private Date fetchDate;
}

package com.banklab.cash.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashVO {

    private Long id;
    private Long memberId;
    private Long cashAmount;
    private Date createdAt;
    private Date updatedAt;
}

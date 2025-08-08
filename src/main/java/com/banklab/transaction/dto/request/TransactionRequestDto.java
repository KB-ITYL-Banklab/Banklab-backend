package com.banklab.transaction.dto.request;


import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDto implements Serializable {
    private String resAccount; // 계좌번호
    private String startDate;   // 조회 시작일
    private String endDate;     // 조회 마지막일
    private String orderBy; // 0: 최신순, 1: 과거순
}

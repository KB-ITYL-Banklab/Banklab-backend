package com.banklab.transaction.dto.request;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDto {
    private String startDate;   // 조회 시작일
    private String endDate;     // 조회 마지막일
    private String orderBy; // 0: 최신순, 1: 과거순
}

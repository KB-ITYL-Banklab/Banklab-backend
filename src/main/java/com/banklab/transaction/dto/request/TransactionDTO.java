package com.banklab.transaction.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String connectedId; // 유저가 발급한 커넥티드 아이디
    private String organization; // 기관 코드
    private String account; // 계좌 번호
    private String startDate;   // 조회 시작일
    private String endDate;     // 조회 마지막일
    private String orderBy; // 0: 최신순, 1: 과거순

}

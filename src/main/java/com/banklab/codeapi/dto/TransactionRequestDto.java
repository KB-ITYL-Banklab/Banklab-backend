package com.banklab.codeapi.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TransactionRequestDto {
    private String connectedId;
    private String organization; // 기관 코드
    private String account; // 계좌 번호
    private String startDate;
    private String endDate;
    private String orderBy; // 0: 최신순, 1: 과거순

    @Override
    public String toString() {
        return "TransactionRequestDto{" +
                "connectedId='" + connectedId + '\'' +
                ", organization='" + organization + '\'' +
                ", account='" + account + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", orderBy='" + orderBy + '\'' +
                '}';
    }
}

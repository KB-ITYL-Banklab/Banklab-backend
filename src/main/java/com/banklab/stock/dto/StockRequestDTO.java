package com.banklab.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDTO {

    // 증권 연동 시 사용
    private String stockCode;
    private String stockId;
    private String stockPassword;
    private String account;
    private String accountPassword;

    // 증권 연동 해제 시 사용
    private String connectedId;
}

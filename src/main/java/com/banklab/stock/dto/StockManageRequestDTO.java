package com.banklab.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockManageRequestDTO {
    private String account;
    private String connectedId;
    private String stockCode;
}

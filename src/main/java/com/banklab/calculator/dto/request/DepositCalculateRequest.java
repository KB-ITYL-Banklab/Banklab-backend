package com.banklab.calculator.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * 예금 계산 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositCalculateRequest {
    private Long principal; // 원금
    private Integer termMonths; // 기간 (개월)
    private Boolean isCompound = true; // 복리 여부 (기본값: 복리)
    private Double rate; // 연이율 (%)

}

package com.banklab.product.domain.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositOption {
    private Long id;
    private String dclsMonth;
    private String finCoNo;
    private String finPrdtCd;
    private String intrRateType;
    private String intrRateTypeNm;
    private Integer saveTrm;
    private BigDecimal intrRate;
    private BigDecimal intrRate2;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

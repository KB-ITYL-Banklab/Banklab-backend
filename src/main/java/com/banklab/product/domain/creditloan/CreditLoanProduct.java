package com.banklab.product.domain.creditloan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLoanProduct {
    private Long id;
    private String dclsMonth;
    private String finCoNo;
    private String korCoNm;
    private String finPrdtCd;
    private String finPrdtNm;
    private String joinWay;
    private String crdtPrdtType;
    private String crdtPrdtTypeNm;
    private String cbName;
    private LocalDate dclsStrtDay;
    private LocalDate dclsEndDay;
    private LocalDateTime finCoSubmDay;

    // 위험도 분석용 추가 필드들
    private String spclCnd; // 우대조건 (분석용)
    private String etcNote; // 기타유의사항 (분석용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

package com.banklab.product.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositOption {
    private Long id;
    private String dclsMonth;  // dcls_month 컬럼에 대응
    private String finCoNo;    // fin_co_no 컬럼에 대응
    private String finPrdtCd;  // fin_prdt_cd 컬럼에 대응
    private String intrRateType;  // intr_rate_type 컬럼에 대응
    private String intrRateTypeNm;  // intr_rate_type_nm 컬럼에 대응
    private Integer saveTrm;  // save_trm 컬럼에 대응
    private BigDecimal intrRate;  // intr_rate 컬럼에 대응
    private BigDecimal intrRate2;  // intr_rate2 컬럼에 대응

}

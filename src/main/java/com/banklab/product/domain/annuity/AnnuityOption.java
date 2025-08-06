package com.banklab.product.domain.annuity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnuityOption {
    private Long id;
    private String dclsMonth;           // 공시 제출월
    private String finCoNo;             // 금융회사 코드
    private String finPrdtCd;           // 금융상품 코드
    private String pnsnRecpTrm;         // 연금수령기간 코드
    private String pnsnRecpTrmNm;       // 연금수령기간명
    private String pnsnEntrAge;         // 가입연령 코드
    private String pnsnEntrAgeNm;       // 가입연령명
    private String monPaymAtm;          // 월납입금액 코드
    private String monPaymAtmNm;        // 월납입금액명
    private String paymPrd;             // 납입기간 코드
    private String paymPrdNm;           // 납입기간명
    private String pnsnStrtAge;         // 연금개시연령 코드
    private String pnsnStrtAgeNm;       // 연금개시연령명
    private BigDecimal pnsnRecpAmt;     // 연금수령액
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정일시
}

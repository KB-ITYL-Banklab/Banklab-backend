package com.banklab.product.domain.mortgage;

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
public class MortgageLoanProduct {
    
    private Long id;
    private String dclsMonth; // 공시 월
    private String finCoNo; // 금융회사 번호
    private String korCoNm; // 금융회사명
    private String finPrdtCd; // 금융상품 코드
    private String finPrdtNm; // 금융상품명
    private String joinWay; // 가입방법
    private String loanInciExpn; // 대출부대비용
    private String erlyRpayFee; // 중도상환수수료
    private String dlyRate; // 연체이자율
    private String loanLmt; // 대출한도
    private LocalDate dclsStrtDay; // 공시 시작일
    private LocalDate dclsEndDay; // 공시 종료일
    private LocalDateTime finCoSubmDay; // 금융회사 제출일
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
}

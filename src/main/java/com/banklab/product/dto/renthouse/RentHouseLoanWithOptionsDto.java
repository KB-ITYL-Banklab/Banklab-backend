package com.banklab.product.dto.renthouse;

import com.banklab.product.domain.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentHouseLoanWithOptionsDto {

    // 상품 정보
    private String dclsMonth; // 공시 월
    private String finCoNo; // 금융회사 번호
    private String finPrdtCd; // 금융상품 코드
    private String korCoNm; // 금융회사명
    private String finPrdtNm; // 금융상품명
    private ProductType productType;

    private String joinWay; // 가입방법
    private String loanInciExpn; // 대출부대비용
    private String erlyRpayFee; // 중도상환수수료
    private String dlyRate; // 연체이자율
    private String loanLmt; // 대출한도
    private String dclsStrtDay; // 공시 시작일
    private String dclsEndDay; // 공시 종료일

    // 옵션 정보
    private List<RentHouseLoanOptionDto> options;

    // 요약 정보
    private int optionCount;       // 옵션 개수
    private BigDecimal minRate;    // 최저 금리
    private BigDecimal maxRate;    // 최고 금리



}

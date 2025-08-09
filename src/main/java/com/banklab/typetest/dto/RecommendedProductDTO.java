package com.banklab.typetest.dto;

import com.banklab.product.domain.ProductType;
import com.banklab.risk.domain.RiskLevel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "추천 상품 정보")
public class RecommendedProductDTO {

    @ApiModelProperty(value = "상품 ID", example = "1")
    private Long productId;

    @ApiModelProperty(value = "상품 타입")
    private ProductType productType;

    @ApiModelProperty(value = "상품명", example = "KB Star 정기예금")
    private String productName;

    @ApiModelProperty(value = "회사명", example = "KB국민은행")
    private String companyName;

    @ApiModelProperty(value = "위험도")
    private RiskLevel riskLevel;

    @ApiModelProperty(value = "위험도 사유", example = "원금보장형 상품")
    private String riskReason;

    // 실제 금리 정보
    @ApiModelProperty(value = "금리 정보", example = "연 2.50~2.50%")
    private String interestRate;    // 실제 금리 정보 (예: "연 2.50~2.50%" 또는 "연 2.45~2.50%")

    //상품 조회용 필드
    @ApiModelProperty(value = "공시월", example = "202408")
    private String dclsMonth;   // 공시월

    @ApiModelProperty(value = "금융회사 코드", example = "0000001")
    private String finCoNo;     // 금융회사 코드

    @ApiModelProperty(value = "금융상품 코드", example = "WR0001B")
    private String finPrdtCd;   // 금융상품 코드
}

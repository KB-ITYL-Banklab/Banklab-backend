package com.banklab.risk.dto;

import com.banklab.product.domain.ProductType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRiskAnalysisRequest {
    private ProductType productType;
    private Long productId;
    
    // Claude API에 전달할 상품 정보
    private String korCoNm;      // 금융회사명
    private String finPrdtNm;    // 상품명
    private String joinWay;      // 가입방법
    private String spclCnd;      // 우대조건
    private String mtrtInt;      // 만기후이자율
    private String etcNote;      // 기타사항

}

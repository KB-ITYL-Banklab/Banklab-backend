package com.banklab.risk.dto;

import com.banklab.risk.domain.RiskLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAnalysisResponse {
    
    @JsonProperty("risk_level")
    private String riskLevel;
    
    @JsonProperty("risk_reason")
    private String riskReason;
    
    private String productCode;
    private Integer riskScore;
    private RiskLevel riskLevelEnum;

    
    public RiskAnalysisResponse(String riskLevel, String riskReason) {
        this.riskLevel = riskLevel;
        this.riskReason = riskReason;
    }
    
    public RiskLevel getRiskLevelEnum() {
        if (this.riskLevel == null || this.riskLevel.isBlank()) {
            return RiskLevel.MEDIUM; // 기본값
        }
        try {
            return RiskLevel.valueOf(this.riskLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RiskLevel.MEDIUM; // 변환 실패 시 기본값
        }
    }
}

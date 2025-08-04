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
        if (riskLevelEnum != null) {
            return riskLevelEnum;
        }
        return RiskLevel.MEDIUM;
    }
}

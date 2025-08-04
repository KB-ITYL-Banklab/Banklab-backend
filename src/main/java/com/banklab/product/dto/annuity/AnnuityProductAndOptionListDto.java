package com.banklab.product.dto.annuity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnuityProductAndOptionListDto {

    @JsonProperty("baseList") // JSON 응답의 "baseList"와 매핑
    private List<AnnuityProductDto> products;

    @JsonProperty("optionList") // JSON 응답의 "optionList"와 매핑
    private List<AnnuityOptionDto> options;

}

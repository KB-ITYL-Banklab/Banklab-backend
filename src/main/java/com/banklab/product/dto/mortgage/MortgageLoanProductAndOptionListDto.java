package com.banklab.product.dto.mortgage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MortgageLoanProductAndOptionListDto {

    @JsonProperty("baseList") // JSON 응답의 "baseList"와 매핑
    private List<MortgageLoanProductDto> products;

    @JsonProperty("optionList") // JSON 응답의 "optionList"와 매핑
    private List<MortgageLoanOptionDto> options;
}

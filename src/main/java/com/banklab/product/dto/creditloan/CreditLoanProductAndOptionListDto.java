package com.banklab.product.dto.creditloan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CreditLoanProductAndOptionListDto {

    @JsonProperty("baseList") // JSON 응답의 "baseList"와 매핑
    private List<CreditLoanProductDto> products;

    @JsonProperty("optionList") // JSON 응답의 "optionList"와 매핑
    private List<CreditLoanOptionDto> options;
}

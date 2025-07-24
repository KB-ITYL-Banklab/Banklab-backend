package com.banklab.product.dto.deposit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DepositProductAndOptionListDto {

    @JsonProperty("baseList") // JSON 응답의 "baseList"와 매핑
    private List<DepositProductDto> products;

    @JsonProperty("optionList") // JSON 응답의 "optionList"와 매핑
    private List<DepositOptionDto> options;
}

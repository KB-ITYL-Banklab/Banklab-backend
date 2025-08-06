package com.banklab.product.dto.renthouse;

import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
import com.banklab.product.dto.savings.SavingsOptionDto;
import com.banklab.product.dto.savings.SavingsProductDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RentHouseLoanProductAndOptionListDto {
    @JsonProperty("baseList") // JSON 응답의 "baseList"와 매핑
    private List<RentHouseLoanProductDto> products;

    @JsonProperty("optionList") // JSON 응답의 "optionList"와 매핑
    private List<RentHouseLoanOptionDto> options;
}
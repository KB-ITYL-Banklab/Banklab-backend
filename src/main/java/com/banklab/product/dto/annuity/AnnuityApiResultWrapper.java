package com.banklab.product.dto.annuity;

import com.banklab.product.dto.creditloan.CreditLoanOptionDto;
import com.banklab.product.dto.creditloan.CreditLoanProductDto;
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
public class AnnuityApiResultWrapper {
    @JsonProperty("result")
    private AnnuityApiResult result;

    @Data
    public static class AnnuityApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "P"

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("max_page_no")
        private Integer maxPageNo;

        @JsonProperty("now_page_no")
        private Integer nowPageNo;

        @JsonProperty("err_cd")
        private String errCd; // "000"

        @JsonProperty("err_msg")
        private String errMsg; // "정상"

        @JsonProperty("baseList")
        private List<AnnuityProductDto> baseList;

        @JsonProperty("optionList")
        private List<AnnuityOptionDto> optionList;
    }
}


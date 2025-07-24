package com.banklab.product.dto.creditloan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CreditLoanApiResultWrapper {
    
    @JsonProperty("result")
    private CreditLoanApiResult result;
    
    @Data
    public static class CreditLoanApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "C"
        
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
        private List<CreditLoanProductDto> baseList;

        @JsonProperty("optionList")
        private List<CreditLoanOptionDto> optionList;
    }
}

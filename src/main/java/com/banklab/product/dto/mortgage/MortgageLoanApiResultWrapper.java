package com.banklab.product.dto.mortgage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MortgageLoanApiResultWrapper {
    
    @JsonProperty("result")
    private MortgageLoanApiResult result;
    
    @Data
    public static class MortgageLoanApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "M"
        
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
        private List<MortgageLoanProductDto> baseList;

        @JsonProperty("optionList")
        private List<MortgageLoanOptionDto> optionList;
    }
}

package com.banklab.product.dto.deposit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DepositApiResultWrapper {
    
    @JsonProperty("result")
    private DepositApiResult result;
    
    @Data
    public static class DepositApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "D"
        
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
        private List<DepositProductDto> baseList;

        @JsonProperty("optionList")
        private List<DepositOptionDto> optionList;
    }
}

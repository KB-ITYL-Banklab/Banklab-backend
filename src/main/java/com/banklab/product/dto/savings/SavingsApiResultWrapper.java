package com.banklab.product.dto.savings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SavingsApiResultWrapper {
    
    @JsonProperty("result")
    private SavingsApiResult result;
    
    @Data
    public static class SavingsApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "S"
        
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
        private List<SavingsProductDto> baseList;

        @JsonProperty("optionList")
        private List<SavingsOptionDto> optionList;
    }
}

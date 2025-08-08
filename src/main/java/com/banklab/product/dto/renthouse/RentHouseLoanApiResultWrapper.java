package com.banklab.product.dto.renthouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RentHouseLoanApiResultWrapper {
    
    @JsonProperty("result")
    private RentHouseLoanApiResult result;
    
    @Data
    public static class RentHouseLoanApiResult {
        @JsonProperty("prdt_div")
        private String prdtDiv; // "R"
        
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
        private List<RentHouseLoanProductDto> baseList;

        @JsonProperty("optionList")
        private List<RentHouseLoanOptionDto> optionList;
    }
}

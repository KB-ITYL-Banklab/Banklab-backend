package com.banklab.codeapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionHistoryVO {
    private Long id;
    private Long accountId;
    private String resAccount;

    @JsonProperty("resAccountTrDate")
    private String transactionDate;

    @JsonProperty("resAccountTrTime")
    private String transactionTime;
    private Long resAccountIn;
    private Long resAccountOut;
    private Long resAfterTranBalance;

    @JsonProperty("resAccountDesc3")
    private String description;
    private Date createDate;
}

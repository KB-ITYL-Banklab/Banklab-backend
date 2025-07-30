package com.banklab.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDTO {
    private Long memberId;
    private String resAccount;
    private Long resAccountIn;
    private Long resAccountOut;
    private Long resAfterTranBalance;
    private String resAccountTrDate;
    private String resAccountTrTime;
    private String resAccountDesc3;
}
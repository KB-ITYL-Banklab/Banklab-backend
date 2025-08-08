package com.banklab.transaction.rabbitMQ.message;


import com.banklab.account.domain.AccountVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryTransactionMessage implements Serializable {
    private Long memberId;
    private AccountVO accountVO;
    private String startDate;
}

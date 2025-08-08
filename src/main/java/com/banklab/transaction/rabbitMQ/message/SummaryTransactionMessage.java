package com.banklab.transaction.rabbitMQ.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryTransactionMessage {
    private Long memberId;
    private String startDate;
}

package com.banklab.transaction.rabbitMQ.message;


import com.banklab.transaction.dto.request.TransactionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchTransactionMessage {
    private Long memberId;
    private TransactionRequestDto request;
}

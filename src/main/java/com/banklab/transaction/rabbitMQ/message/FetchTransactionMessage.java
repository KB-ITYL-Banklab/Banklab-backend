package com.banklab.transaction.rabbitMQ.message;


import com.banklab.transaction.dto.request.TransactionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchTransactionMessage implements Serializable {
    private Long memberId;
    private TransactionRequestDto request;
}

package com.banklab.transaction.rabbitMQ.message;

import com.banklab.transaction.domain.TransactionHistoryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorizeTransactionMessage {
    private Long memberId;
    private Long accountId;
    private String startDate;
    private List<TransactionHistoryVO> transactions;

}

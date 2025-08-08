package com.banklab.transaction.rabbitMQ.message;

import com.banklab.transaction.domain.TransactionHistoryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiCategorizeMessage {
    private Long memberId;
    private Long accountId;
    private String startDate;
    private Set<String> descriptions;
    private List<TransactionHistoryVO> transactions;

}

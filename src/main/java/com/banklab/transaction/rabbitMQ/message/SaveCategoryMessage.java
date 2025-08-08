package com.banklab.transaction.rabbitMQ.message;


import com.banklab.account.domain.AccountVO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveCategoryMessage implements Serializable {
    private Long memberId;
    private AccountVO account;
    private String startDate;
    private List<TransactionHistoryVO> transactions;
}

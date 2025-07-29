package com.banklab.transaction.service;

import com.banklab.transaction.dto.request.TransactionRequestDto;

public interface AsyncTransactionService {
    void getTransactions(long memberId, TransactionRequestDto request);

}

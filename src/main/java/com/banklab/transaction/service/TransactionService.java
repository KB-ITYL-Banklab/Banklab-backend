package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionService {
    int saveTransactionList(List<TransactionHistoryVO> transactionVOList);

    LocalDate getLastTransactionDay(Long memberId, String account);

    //    TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request);
    void getTransactions(long memberId, TransactionRequestDto request);


    SummaryDTO getSummary(Long memberId, Date startDate, Date endDate);

    MonthlySummaryDTO getMonthlySummary(Long memberId, Date startDate, Date endDate);

    List<DailyExpenseDTO> getDailyExpense(Long memberId, Date startDate, Date endDate);

    List<CategoryExpenseDTO> getCategoryExpense(Long memberId, Date startDate, Date endDate);
}

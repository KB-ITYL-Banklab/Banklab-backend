package com.banklab.transaction.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionService {

    /**
     * CODEF API에서 불러온 계좌 거래 내역 저장
     *
     * @param transaction: 해당 계좌 거래 내역
     * @return 저장된 거래 내역 수
     */
    int saveTransaction(TransactionHistoryVO transaction);

    int saveTransactionList(List<TransactionHistoryVO> transactionVOList);
    LocalDate getLastTransactionDay(Long memberId);

    int getTransactions(long memberId, TransactionRequestDto request);

    public SummaryDTO getSummary(Long memberId, Date startDate, Date endDate);
    public MonthlySummaryDTO getMonthlySummary(Date startDate, Date endDate, String account) ;
    public List<DailyExpenseDTO> getDailyExpense(Date startDate, Date endDate, String account);
    public List<CategoryExpenseDTO> getCategoryExpense(Date startDate, Date endDate, String account);
}

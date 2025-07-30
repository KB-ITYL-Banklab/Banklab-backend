package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.dto.response.TransactionDetailDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionService {
    void saveTransactionList(Long memberId, AccountVO account, List<TransactionHistoryVO> transactionVOList);

    LocalDate getLastTransactionDay(Long memberId, String account);

    void updateCategories(List<TransactionHistoryVO> transactions);
    //    TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request);


    SummaryDTO getSummary(Long memberId, Date startDate, Date endDate);

    MonthlySummaryDTO getMonthlySummary(Long memberId, Date startDate, Date endDate);

    List<DailyExpenseDTO> getDailyExpense(Long memberId, Date startDate, Date endDate);

    List<CategoryExpenseDTO> getCategoryExpense(Long memberId, Date startDate, Date endDate);


    /**
     * 계좌 ID로 거래내역 상세 조회 (계좌 소유권 검증 포함)
     *
     * @param memberId  회원 ID
     * @param accountId 계좌 ID (account 테이블의 PK)
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 거래내역 상세 DTO 리스트
     */
    public List<TransactionDetailDTO> getTransactionDetailsByAccountId(Long memberId, Long accountId, Date startDate, Date endDate);
}

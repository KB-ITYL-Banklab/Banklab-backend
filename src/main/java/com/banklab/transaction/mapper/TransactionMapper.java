package com.banklab.transaction.mapper;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.TransactionDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionMapper {
    int saveTransaction(TransactionHistoryVO transaction);
    int saveTransactionList(List<TransactionHistoryVO> list);
    LocalDate getLastTransactionDate(@Param("memberId") Long memberId);


    MonthlySummaryDTO getMonthlySummary(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<DailyExpenseDTO> getDailyExpense(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    List<CategoryExpenseDTO> getExpensesByCategory(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


//    MonthlySummaryDTO getMonthlySummary(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("resAccount") String resAccount);
//    List<DailyExpenseDTO> getDailyExpense(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("resAccount") String resAccount);
//    List<CategoryExpenseDTO> getExpensesByCategory(@Param("startDate") Date startDate, @Param("endDate") Date endDate,@Param("resAccount") String resAccount);

    /**
     * 계좌 ID로 거래내역 상세 조회 (DTO 반환)
     */
    List<TransactionDetailDTO> getTransactionDetailsByAccountId(
            @Param("memberId") Long memberId,
            @Param("resAccount") String resAccount,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}

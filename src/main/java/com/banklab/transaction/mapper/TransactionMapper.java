package com.banklab.transaction.mapper;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionMapper {
    int saveTransaction(TransactionHistoryVO transaction);

    int saveTransactionList(List<TransactionHistoryVO> list);

    LocalDate getLastTransactionDate(@Param("memberId") Long memberId, @Param("resAccount") String resAccount);


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
}

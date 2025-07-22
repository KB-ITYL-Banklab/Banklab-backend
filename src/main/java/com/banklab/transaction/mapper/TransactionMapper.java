package com.banklab.transaction.mapper;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface TransactionMapper {

    int saveTransaction(TransactionHistoryVO transaction);
    int saveTransactionList(List<TransactionHistoryVO> list);

    MonthlySummaryDTO getMonthlySummary(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("resAccount") String resAccount);
    List<DailyExpenseDTO> getDailyExpense(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("resAccount") String resAccount);
    List<CategoryExpenseDTO> getExpensesByCategory(@Param("startDate") Date startDate, @Param("endDate") Date endDate,@Param("resAccount") String resAccount);
}

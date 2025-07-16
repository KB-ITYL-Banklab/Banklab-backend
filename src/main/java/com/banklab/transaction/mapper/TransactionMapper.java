package com.banklab.transaction.mapper;

import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransactionMapper {

    MonthlySummaryDTO getMonthlySummary(@Param("year") int year, @Param("month") int month, @Param("resAccount") String resAccount);
    List<DailyExpenseDTO> getDailyExpense(@Param("year") int year, @Param("month") int month, @Param("resAccount") String resAccount);
}

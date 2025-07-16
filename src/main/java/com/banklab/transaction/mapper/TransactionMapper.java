package com.banklab.transaction.mapper;

import com.banklab.codeapi.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TransactionMapper {

    MonthlySummaryDTO getMonthlySummary(@Param("year") int year, @Param("month") int month, @Param("resAccount") String resAccount);
    List<DailyExpenseDTO> getDailyExpense(@Param("year") int year, @Param("month") int month, @Param("resAccount") String resAccount);
}

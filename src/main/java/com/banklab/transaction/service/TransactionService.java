package com.banklab.transaction.service;

import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;

    public MonthlySummaryDTO getMonthlySummary(int year, int month, String account) {
        MonthlySummaryDTO monthlySummary = transactionMapper.getMonthlySummary(year, month, account);
        if (monthlySummary == null) {
            return new MonthlySummaryDTO();
        }
        return monthlySummary;
    }

    public List<DailyExpenseDTO> getDailyExpense(int year, int month, String account) {
        List<DailyExpenseDTO> list = transactionMapper.getDailyExpense(year, month, account).stream().toList();
        return list;
    }



}

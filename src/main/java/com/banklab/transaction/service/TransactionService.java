package com.banklab.transaction.service;

import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;

    public MonthlySummaryDTO getMonthlySummary(int year, int month, String account) {
        return transactionMapper.getMonthlySummary(year, month, account);
    }

    public List<DailyExpenseDTO> getDailyExpense(int year, int month, String account) {
        List<DailyExpenseDTO> list = transactionMapper.getDailyExpense(year, month, account).stream().toList();
        for(DailyExpenseDTO dailyExpenseDTO : list) {
            System.out.println(dailyExpenseDTO.toString());
        }
        return list;
    }



}

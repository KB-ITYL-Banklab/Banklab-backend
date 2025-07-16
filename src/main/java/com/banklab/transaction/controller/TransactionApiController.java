package com.banklab.transaction.controller;

import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.dto.SummaryDTO;
import com.banklab.transaction.dto.WeeklyExpenseDTO;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Month;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;


    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("account") String account
    ){

        return ResponseEntity.ok(transactionService.getSummary(year, month, account));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("account") String account
            ) {


        return  ResponseEntity.ok(transactionService.getMonthlySummary(year, month, account));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyExpenseDTO>> getDailySummary(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("account") String account
    ) {


        return  ResponseEntity.ok(transactionService.getDailyExpense(year, month, account));
    }




}

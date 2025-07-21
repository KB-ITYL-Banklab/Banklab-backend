package com.banklab.transaction.controller;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.dto.DailyExpenseDTO;
import com.banklab.transaction.dto.MonthlySummaryDTO;
import com.banklab.transaction.dto.SummaryDTO;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam("account") String account
    ) {

        return ResponseEntity.ok(transactionService.getSummary(startDate, endDate, account));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam("account") String account
    ) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(startDate,endDate, account));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyExpenseDTO>> getDailySummary(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam("account") String account
    ) {
        return ResponseEntity.ok(transactionService.getDailyExpense(startDate,endDate, account));
    }

    @GetMapping("/category")
    public List<CategoryExpenseDTO> getCategoryExpenses(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return transactionService.getCategoryExpense(startDate, endDate);
    }


}

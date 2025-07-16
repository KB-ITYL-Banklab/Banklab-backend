package com.banklab.transaction.controller;

import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/monthly-transaction")
    public ResponseEntity<Map<String, Long>> getSummary(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month){

        Map<String, Long> monthlySummary = transactionService.getTransactionSummaryForMonth(year, month);
        return ResponseEntity.ok(monthlySummary);

    }


}

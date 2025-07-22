package com.banklab.transaction.controller;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.service.TransactionServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Log4j2
public class TransactionApiController {

    private final TransactionServiceImpl transactionService;

    @PostMapping("/transaction-list")
    @ApiOperation(value = "거래 내역 조회", notes = "사용자와 연동된 계좌 거래 내역 조회")
    public ResponseEntity<Map<String, Object>> getTransactionList(
            @RequestParam Long memberId,
            @RequestBody TransactionRequestDto request) {
        Map<String, Object> response = new HashMap<>();

        int savedRows = transactionService.getTransactions(memberId, request);

        response.put("저장된 전체 거래 내역", savedRows);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary(
            @RequestParam Long memberId,
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate)
     {
        // 기본값 설정: 오늘이 포함된 달의 시작일과 종료일
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }

        return ResponseEntity.ok(transactionService.getSummary(memberId, startDate, endDate));
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
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam("account") String account
            ) {

        // 기본값 설정: 오늘이 포함된 달의 시작일과 종료일
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }

        return transactionService.getCategoryExpense(startDate, endDate,account);
    }


}

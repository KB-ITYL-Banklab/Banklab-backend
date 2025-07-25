package com.banklab.transaction.controller;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.service.TransactionService;
import com.banklab.transaction.service.TransactionServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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


    /**
     * 표준화된 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    /**
     * 표준화된 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", errorCode);
        return response;
    }

    private final TransactionService transactionService;

    @PostMapping("/transaction-list")
    @ApiOperation(value = "거래 내역 조회", notes = "사용자와 연동된 계좌 거래 내역 조회")
    public ResponseEntity<Map<String, Object>> getTransactionList(
            @RequestParam Long memberId,
            @RequestBody TransactionRequestDto request) {
        Map<String, Object> response = new HashMap<>();

        try{
            int savedRows = transactionService.getTransactions(memberId, request);
            response.put("memberId", memberId);
            response.put("savedRows", savedRows);

            return ResponseEntity.ok(createSuccessResponse("거래 내역을 불러왔습니다.",response));
        }catch (Exception e){
            log.error("거래 내역 api 호출 중 오류 발생",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("거래 내역을 불러오는 중 오류가 발생했습니다","INTERNAL_ERROR"));
        }
    }

    @ApiOperation(value = "거래 내역 db 존재 유무 조회", notes = "사용자의 특정 계좌 거래 내역 db 확인")
    @GetMapping("/transaction-list")
    public ResponseEntity<Map<String, Object>> checkTransactionHisOfMember(
            @RequestParam Long memberId,
            @RequestParam String account){

        Map<String, Object> response = new HashMap<>();
        try{
            LocalDate lastTransactionDay = transactionService.getLastTransactionDay(memberId, account);
            response.put("lastDate", lastTransactionDay);

            return ResponseEntity.ok(createSuccessResponse("거래 내역 db 조회 성공",response));
        }catch (Exception e){
            log.error("특정 계좌 거래 내역 db 조회 중 오류 발생",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("거래 내역 db 조회 중 오류가 발생했습니다.","INTERNAL_ERROR"));
        }
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

         System.out.println(startDate+" "+endDate);

        return ResponseEntity.ok(transactionService.getSummary(memberId, startDate, endDate));
    }

    /** getSumamry 통합 전 코드
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
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
     */


}

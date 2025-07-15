package com.banklab.codeapi.controller;

import com.banklab.codeapi.dto.TransactionRequestDto;
import com.banklab.codeapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;

    @PostMapping("/transaction-list")
    public ResponseEntity<String> fetchTransactionList(@RequestBody TransactionRequestDto request) {
        System.out.println("Fetching transaction list");
        transactionService.fetchAndSaveTransactions(request);
        return ResponseEntity.ok("거래내역 저장 완료");
    }


}

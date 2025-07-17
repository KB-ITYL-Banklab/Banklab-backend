package com.banklab.codeapi.controller;

import com.banklab.codeapi.dto.TransactionRequestDto;
import com.banklab.codeapi.service.CodeapiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class CodeApiController {

    private final CodeapiService codeapiService;

    @PostMapping("/transaction-list")
    public ResponseEntity<String> fetchTransactionList(@RequestBody TransactionRequestDto request) {
        codeapiService.fetchAndSaveTransactions(request);
        return ResponseEntity.ok("거래내역 저장 완료");
    }


}

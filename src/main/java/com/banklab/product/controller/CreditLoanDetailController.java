package com.banklab.product.controller;

import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;
import com.banklab.product.service.CreditLoanDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 신용대출 상품 전용 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/creditloan")
@RequiredArgsConstructor
public class CreditLoanDetailController {

    private final CreditLoanDetailService creditLoanDetailService;

    /**
     * 특정 신용대출 상품의 모든 정보 조회
     */
    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}")
    public ResponseEntity<CreditLoanWithOptionsDto> getCreditLoanWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {
        
        try {
            CreditLoanWithOptionsDto result = creditLoanDetailService.getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);
            
            if (result == null) {
                log.warn("신용대출 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                        dclsMonth, finCoNo, finPrdtCd);
                return ResponseEntity.notFound().build();
            }
            
            log.info("신용대출 상품 옵션 조회 성공: 상품명={}, 옵션수={}", result.getFinPrdtNm(), result.getOptionCount());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("신용대출 상품 옵션 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

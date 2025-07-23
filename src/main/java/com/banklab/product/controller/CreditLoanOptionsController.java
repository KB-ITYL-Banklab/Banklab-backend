package com.banklab.product.controller;

import com.banklab.product.dto.creditloan.CreditLoanWithOptionsDto;
import com.banklab.product.service.CreditLoanOptionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 신용대출 상품 전용 옵션 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/creditloan")
@RequiredArgsConstructor
public class CreditLoanOptionsController {

    private final CreditLoanOptionsService creditLoanOptionsService;

    /**
     * 특정 신용대출 상품의 모든 옵션 조회
     * GET /api/creditloan/{dclsMonth}/{finCoNo}/{finPrdtCd}/options
     */
    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}/options")
    public ResponseEntity<CreditLoanWithOptionsDto> getCreditLoanWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {
        
        log.info("신용대출 상품 옵션 조회 요청: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                dclsMonth, finCoNo, finPrdtCd);
        
        try {
            CreditLoanWithOptionsDto result = creditLoanOptionsService.getCreditLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);
            
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

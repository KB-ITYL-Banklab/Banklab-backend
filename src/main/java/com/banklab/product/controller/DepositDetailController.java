package com.banklab.product.controller;

import com.banklab.product.dto.deposit.DepositWithOptionsDto;
import com.banklab.product.service.DepositDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 예금 상품 전용 옵션 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/deposit")
@RequiredArgsConstructor
public class DepositDetailController {

    private final DepositDetailService depositDetailService;

    /**
     * 특정 예금 상품의 모든 옵션 조회
     * dcls_month + fin_co_no + fin_prdt_cd로 상품을 고유하게 식별
     * GET /api/deposit/{dclsMonth}/{finCoNo}/{finPrdtCd}
     */
    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}")
    public ResponseEntity<DepositWithOptionsDto> getDepositWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {
        
        log.info("예금 상품 옵션 조회 요청: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                dclsMonth, finCoNo, finPrdtCd);
        
        try {
            DepositWithOptionsDto result = depositDetailService.getDepositWithOptions(dclsMonth, finCoNo, finPrdtCd);
            
            if (result == null) {
                log.warn("예금 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                        dclsMonth, finCoNo, finPrdtCd);
                return ResponseEntity.notFound().build();
            }
            
            log.info("예금 상품 옵션 조회 성공: 상품명={}, 옵션수={}", result.getFinPrdtNm(), result.getOptionCount());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("예금 상품 옵션 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

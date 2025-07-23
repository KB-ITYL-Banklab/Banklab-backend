package com.banklab.product.controller;

import com.banklab.product.dto.savings.SavingsWithOptionsDto;
import com.banklab.product.service.SavingsOptionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 적금 상품 전용 옵션 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsOptionsController {

    private final SavingsOptionsService savingsOptionsService;

    /**
     * 특정 적금 상품의 모든 옵션 조회
     * GET /api/savings/{dclsMonth}/{finCoNo}/{finPrdtCd}/options
     */
    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}/options")
    public ResponseEntity<SavingsWithOptionsDto> getSavingsWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {

        System.out.println("dclsMonth: " + dclsMonth+"finCoNo: " + finCoNo+"finPrdtCd: " + finPrdtCd);
        log.info("적금 상품 옵션 조회 요청: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                dclsMonth, finCoNo, finPrdtCd);

        try {
            SavingsWithOptionsDto result = savingsOptionsService.getSavingsWithOptions(dclsMonth, finCoNo, finPrdtCd);

            if (result == null) {
                log.warn("적금 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                        dclsMonth, finCoNo, finPrdtCd);
                return ResponseEntity.notFound().build();
            }

            log.info("적금 상품 옵션 조회 성공: 상품명={}, 옵션수={}", result.getFinPrdtNm(), result.getOptionCount());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("적금 상품 옵션 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


package com.banklab.product.controller;

import com.banklab.product.dto.renthouse.RentHouseLoanWithOptionsDto;
import com.banklab.product.service.renthouse.RentHouseLoanDetailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rent-house-loan")
@RequiredArgsConstructor
@Log4j2
public class RentHouseLoanDetailController {
    
    private final RentHouseLoanDetailService rentHouseLoanDetailService;

    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}")
    public ResponseEntity<RentHouseLoanWithOptionsDto> getRentHouseLoanWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {

        log.info("주택자금대출 상품 옵션 조회 요청: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                dclsMonth, finCoNo, finPrdtCd);

        try {
            RentHouseLoanWithOptionsDto result = rentHouseLoanDetailService.getRentHouseLoanWithOptions(dclsMonth, finCoNo, finPrdtCd);

            if (result == null) {
                log.warn("주택자금대출 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                        dclsMonth, finCoNo, finPrdtCd);
                return ResponseEntity.notFound().build();
            }

            log.info("주택자금대출 상품 옵션 조회 성공: 상품명={}, 옵션수={}", result.getFinPrdtNm(), result.getOptionCount());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("주택자금대출 상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

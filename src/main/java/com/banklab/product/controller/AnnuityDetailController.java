package com.banklab.product.controller;

import com.banklab.product.dto.annuity.AnnuityWithOptionsDto;
import com.banklab.product.service.annuity.AnnuityDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/annuity")
@RequiredArgsConstructor
public class AnnuityDetailController {

    @Autowired
    private AnnuityDetailService annuityDetailService;

    /**
     * 모든 연금저축 상품과 옵션 정보를 조회합니다.
     * 
     * @return 연금저축 상품과 옵션 정보
     */
    @GetMapping("/{dclsMonth}/{finCoNo}/{finPrdtCd}")
    public ResponseEntity<AnnuityWithOptionsDto> getAnnuityWithOptions(
            @PathVariable String dclsMonth,
            @PathVariable String finCoNo,
            @PathVariable String finPrdtCd) {

        try {
            AnnuityWithOptionsDto result = annuityDetailService.getAnnuityProductsWithOptions(dclsMonth, finCoNo, finPrdtCd);

            if (result == null) {
                log.warn("연금저축 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                        dclsMonth, finCoNo, finPrdtCd);
                return ResponseEntity.notFound().build();
            }

            log.info("연금저축 상품 옵션 조회 성공: 상품명={}, 옵션수={}", result.getFinPrdtNm(), result.getOptionCount());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("연금저축 상품 옵션 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

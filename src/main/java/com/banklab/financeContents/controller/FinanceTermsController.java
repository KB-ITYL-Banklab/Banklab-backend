package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.FinanceTermsResponse;
import com.banklab.financeContents.service.FinanceTermsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 금융용어 컨트롤러
 * 금융용어 검색 API를 제공하는 컨트롤러
 *
 * <검색예시>
 *
 *GDP 검색:
 * http://localhost:8080/api/terms/search?term=GDP
 *
 * CPI 검색:
 * http://localhost:8080/api/terms/search?term=CPI
 *
 * 금리 검색:
 * http://localhost:8080/api/terms/search?term=금리
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Api(tags = "금융용어 API", description = "SEIBRO 금융용어 정보 조회")
public class FinanceTermsController {
    
    private final FinanceTermsService financeTermsService;
    
    /**
     * 금융용어 검색 API
     * 
     * @param term 검색할 금융용어
     * @return 금융용어 정보
     */
    @GetMapping("/search")
    @ApiOperation(value = "금융용어 검색", notes = "입력한 금융용어에 대한 정의를 검색합니다.")
    public ResponseEntity<FinanceTermsResponse> searchFinanceTerm(
            @ApiParam(value = "검색할 금융용어", required = true, example = "GDP")
            @RequestParam("term") String term) {
        
        try {
            log.info("📖 금융용어 검색 요청: {}", term);
            
            FinanceTermsResponse response = financeTermsService.getFinanceTerm(term);
            
            if (response.isSuccess()) {
                log.info("✅ 금융용어 검색 성공: {}", term);
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ 금융용어 검색 실패: {} - {}", term, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ 금융용어 검색 중 오류 발생: {}", e.getMessage(), e);
            FinanceTermsResponse errorResponse = FinanceTermsResponse.failure(term, "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

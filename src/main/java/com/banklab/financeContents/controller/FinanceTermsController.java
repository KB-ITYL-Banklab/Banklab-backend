package com.banklab.financeContents.controller;

import com.banklab.financeContents.service.FinanceTermCsvService;
import com.banklab.financeContents.domain.FinanceTermVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 금융용어 컨트롤러 (DB 전용)
 * CSV 데이터베이스에서 금융용어를 조회하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Api(tags = "금융용어 API")
public class FinanceTermsController {
    
    private final FinanceTermCsvService financeTermCsvService;

    
    /**
     * 금융용어 검색 API (DB에서 검색) - 핵심 기능
     * 
     * @param term 검색할 금융용어
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 20)
     * @return 검색 결과
     */
    @GetMapping("/search")
    @ApiOperation(value = "금융용어 검색", notes = "데이터베이스에 저장된 금융용어에서 검색합니다.")
    public ResponseEntity<Map<String, Object>> searchFinanceTermInDB(
            @ApiParam(value = "검색할 금융용어", required = true, example = "가구")
            @RequestParam("term") String term,
            @ApiParam(value = "페이지 번호", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ApiParam(value = "페이지 크기", example = "20")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔍 DB 금융용어 검색 요청: {} (page={}, size={})", term, page, size);
            
            // 검색어가 비어있는 경우 처리
            if (term == null || term.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "검색어를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 페이지 파라미터 검증
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            
            List<FinanceTermVO> searchResults = financeTermCsvService.searchByTerm(term.trim());
            
            // 페이징 처리
            int totalCount = searchResults.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalCount);
            
            List<FinanceTermVO> pagedResults = totalCount > 0 && startIndex < totalCount 
                ? searchResults.subList(startIndex, endIndex) 
                : List.of();
            
            log.info("✅ DB 금융용어 검색 완료: {}개 결과 (전체 {}개, {}페이지 중 {}페이지)", 
                    pagedResults.size(), totalCount, totalPages, page);
            
            response.put("success", true);
            response.put("message", "검색이 완료되었습니다.");
            response.put("searchTerm", term.trim());
            response.put("pagination", Map.of(
                "currentPage", page,
                "pageSize", size,
                "totalCount", totalCount,
                "totalPages", totalPages,
                "hasNext", page < totalPages,
                "hasPrevious", page > 1
            ));
            response.put("data", pagedResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ DB 금융용어 검색 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "검색 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 금융용어 상세 조회 API
     * 
     * @param term 조회할 용어 (정확한 일치)
     * @return 금융용어 상세 정보
     */
    @GetMapping("/detail")
    @ApiOperation(value = "금융용어 상세 조회", notes = "정확한 용어명으로 금융용어 상세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getFinanceTermDetail(
            @ApiParam(value = "조회할 용어", required = true, example = "GDP")
            @RequestParam("term") String term) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🎯 금융용어 상세 조회 요청: {}", term);
            
            if (term == null || term.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "용어를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            FinanceTermVO termDetail = financeTermCsvService.getTermByExactMatch(term.trim());
            
            if (termDetail != null) {
                log.info("✅ 금융용어 상세 조회 성공: {}", term);
                response.put("success", true);
                response.put("message", "용어 조회가 완료되었습니다.");
                response.put("data", termDetail);
            } else {
                log.info("⚠️ 금융용어 상세 조회 실패: {} - 용어를 찾을 수 없음", term);
                response.put("success", false);
                response.put("message", "해당 용어를 찾을 수 없습니다.");
                response.put("searchTerm", term.trim());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 금융용어 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 키워드로 금융용어 조회 API
     * 
     * @param keyword 조회할 키워드 (정확한 일치)
     * @return 금융용어 상세 정보
     */
    @GetMapping("/keyword")
    @ApiOperation(value = "키워드로 금융용어 조회", notes = "정확한 키워드로 금융용어 상세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getFinanceTermByKeyword(
            @ApiParam(value = "조회할 키워드", required = true, example = "ABCP")
            @RequestParam("keyword") String keyword) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔑 키워드로 금융용어 조회 요청: {}", keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "키워드를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            FinanceTermVO termDetail = financeTermCsvService.getTermByKeyword(keyword.trim());
            
            if (termDetail != null) {
                log.info("✅ 키워드로 금융용어 조회 성공: {}", keyword);
                response.put("success", true);
                response.put("message", "키워드 조회가 완료되었습니다.");
                response.put("data", termDetail);
            } else {
                log.info("⚠️ 키워드로 금융용어 조회 실패: {} - 키워드를 찾을 수 없음", keyword);
                response.put("success", false);
                response.put("message", "해당 키워드를 찾을 수 없습니다.");
                response.put("searchKeyword", keyword.trim());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 키워드로 금융용어 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 저장된 모든 금융용어 조회 API
     * 
     * @return 저장된 금융용어 리스트
     */
    @GetMapping("/list")
    @ApiOperation(value = "저장된 금융용어 조회", notes = "데이터베이스에 저장된 모든 금융용어를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getStoredFinanceTerms() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📋 저장된 금융용어 조회 요청");
            
            List<FinanceTermVO> storedTerms = financeTermCsvService.getAllTerms();
            int totalCount = financeTermCsvService.getTermsCount();
            
            log.info("✅ 저장된 금융용어 조회 성공: {}개", totalCount);
            
            response.put("success", true);
            response.put("message", "저장된 금융용어 조회가 완료되었습니다.");
            response.put("totalCount", totalCount);
            response.put("data", storedTerms);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 저장된 금융용어 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 페이징된 금융용어 목록 조회 API
     * 
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 20)
     * @param subject 주제 필터 (선택사항)
     * @return 페이징된 금융용어 목록
     */
    @GetMapping("/list-paged")
    @ApiOperation(value = "페이징된 금융용어 목록 조회", notes = "페이징 처리된 금융용어 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getPagedFinanceTerms(
            @ApiParam(value = "페이지 번호", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ApiParam(value = "페이지 크기", example = "20")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "주제 필터", example = "경제")
            @RequestParam(value = "subject", required = false) String subject) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📄 페이징된 금융용어 목록 조회 요청: page={}, size={}, subject={}", page, size, subject);
            
            // 페이지 파라미터 검증
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            
            List<FinanceTermVO> terms;
            int totalCount;
            
            if (subject != null && !subject.trim().isEmpty()) {
                // 주제 필터가 있는 경우
                terms = financeTermCsvService.searchBySubject(subject.trim());
                totalCount = terms.size();
                
                // 수동 페이징
                int startIndex = (page - 1) * size;
                int endIndex = Math.min(startIndex + size, totalCount);
                terms = totalCount > 0 && startIndex < totalCount 
                    ? terms.subList(startIndex, endIndex) 
                    : List.of();
            } else {
                // 전체 조회
                totalCount = financeTermCsvService.getTermsCount();
                int offset = (page - 1) * size;
                terms = financeTermCsvService.getTermsWithPaging(offset, size);
            }
            
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            log.info("✅ 페이징된 금융용어 목록 조회 완료: {}개 결과 (전체 {}개, {}페이지 중 {}페이지)", 
                    terms.size(), totalCount, totalPages, page);
            
            response.put("success", true);
            response.put("message", "목록 조회가 완료되었습니다.");
            response.put("pagination", Map.of(
                "currentPage", page,
                "pageSize", size,
                "totalCount", totalCount,
                "totalPages", totalPages,
                "hasNext", page < totalPages,
                "hasPrevious", page > 1
            ));
            response.put("filter", Map.of(
                "subject", subject != null ? subject.trim() : ""
            ));
            response.put("data", terms);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 페이징된 금융용어 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 금융용어 랜덤 조회 API
     * 
     * @param count 조회할 개수 (기본값: 5, 최대: 20)
     * @return 랜덤 금융용어 리스트
     */
    @GetMapping("/random")
    @ApiOperation(value = "랜덤 금융용어 조회", notes = "랜덤하게 금융용어를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getRandomFinanceTerms(
            @ApiParam(value = "조회할 개수", example = "5")
            @RequestParam(value = "count", defaultValue = "5") int count) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🎲 랜덤 금융용어 조회 요청: {}개", count);
            
            // 개수 제한
            if (count < 1) count = 1;
            if (count > 20) count = 20;
            
            // 전체 개수 조회
            int totalCount = financeTermCsvService.getTermsCount();
            
            if (totalCount == 0) {
                response.put("success", false);
                response.put("message", "저장된 금융용어가 없습니다. 데이터베이스를 확인해주세요.");
                return ResponseEntity.ok(response);
            }
            
            // 랜덤 오프셋 생성 및 조회
            List<FinanceTermVO> randomTerms;
            if (totalCount <= count) {
                // 전체 개수가 요청한 개수보다 적으면 모두 조회
                randomTerms = financeTermCsvService.getAllTerms();
            } else {
                // 랜덤 시작점에서 조회
                int randomOffset = (int) (Math.random() * (totalCount - count));
                randomTerms = financeTermCsvService.getTermsWithPaging(randomOffset, count);
            }
            
            log.info("✅ 랜덤 금융용어 조회 완료: {}개", randomTerms.size());
            
            response.put("success", true);
            response.put("message", "랜덤 용어 조회가 완료되었습니다.");
            response.put("requestedCount", count);
            response.put("returnedCount", randomTerms.size());
            response.put("data", randomTerms);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 랜덤 금융용어 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

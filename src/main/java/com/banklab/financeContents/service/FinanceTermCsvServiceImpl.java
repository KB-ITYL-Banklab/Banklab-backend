package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceTermVO;
import com.banklab.financeContents.mapper.FinanceTermMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 금융용어 CSV 서비스 구현체
 * 데이터베이스에 저장된 금융용어 관련 서비스를 구현하는 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceTermCsvServiceImpl implements FinanceTermCsvService {
    
    private final FinanceTermMapper financeTermMapper;
    
    @Override
    public List<FinanceTermVO> getAllTerms() {
        try {
            log.info("📋 모든 금융용어 조회 시작");
            List<FinanceTermVO> terms = financeTermMapper.selectAllTerms();
            log.info("✅ 모든 금융용어 조회 완료: {}개", terms.size());
            
            // 디버깅용 로그 추가
            if (terms.size() == 1) {
                log.warn("⚠️ 단 1개의 결과만 반환됨 - 잠재적 문제");
                log.info("반환된 데이터: {}", terms.get(0).getTitle());
            }
            
            return terms;
        } catch (Exception e) {
            log.error("❌ 모든 금융용어 조회 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 빈 리스트 반환
            return List.of();
        }
    }
    
    @Override
    public int getTermsCount() {
        try {
            log.info("📊 금융용어 개수 조회 시작");
            int count = financeTermMapper.selectTermsCount();
            log.info("✅ 금융용어 개수 조회 완료: {}개", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 금융용어 개수 조회 중 오류 발생: {}", e.getMessage(), e);
            // 테이블이 없거나 오류가 발생한 경우 0을 반환
            return 0;
        }
    }
    
    @Override
    public List<FinanceTermVO> searchByTerm(String term) {
        try {
            log.info("🔍 용어 검색 시작: {}", term);
            List<FinanceTermVO> results = financeTermMapper.searchByTerm(term);
            log.info("✅ 용어 검색 완료: {}개 결과", results.size());
            return results;
        } catch (Exception e) {
            log.error("❌ 용어 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<FinanceTermVO> searchBySubject(String subject) {
        try {
            log.info("📂 주제 검색 시작: {}", subject);
            List<FinanceTermVO> results = financeTermMapper.searchBySubject(subject);
            log.info("✅ 주제 검색 완료: {}개 결과", results.size());
            return results;
        } catch (Exception e) {
            log.error("❌ 주제 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public FinanceTermVO getTermByExactMatch(String term) {
        try {
            log.info("🎯 정확한 용어 조회 시작: {}", term);
            FinanceTermVO result = financeTermMapper.selectByExactTerm(term);
            log.info("✅ 정확한 용어 조회 완료: {}", result != null ? "발견" : "없음");
            return result;
        } catch (Exception e) {
            log.error("❌ 정확한 용어 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public FinanceTermVO getTermByKeyword(String keyword) {
        try {
            log.info("🔑 키워드 조회 시작: {}", keyword);
            FinanceTermVO result = financeTermMapper.selectByKeyword(keyword);
            log.info("✅ 키워드 조회 완료: {}", result != null ? "발견" : "없음");
            return result;
        } catch (Exception e) {
            log.error("❌ 키워드 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<FinanceTermVO> getTermsWithPaging(int offset, int limit) {
        try {
            log.info("📄 페이징 조회 시작: offset={}, limit={}", offset, limit);
            List<FinanceTermVO> results = financeTermMapper.selectTermsWithPaging(offset, limit);
            log.info("✅ 페이징 조회 완료: {}개 결과", results.size());
            return results;
        } catch (Exception e) {
            log.error("❌ 페이징 조회 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 빈 리스트 반환
            return List.of();
        }
    }
}

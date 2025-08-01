package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceTermVO;

import java.util.List;

/**
 * 금융용어 CSV 서비스 인터페이스
 * 데이터베이스에 저장된 금융용어 관련 서비스를 제공하는 인터페이스
 */
public interface FinanceTermCsvService {
    
    /**
     * 모든 금융용어 조회
     * @return 금융용어 리스트
     */
    List<FinanceTermVO> getAllTerms();
    
    /**
     * 금융용어 개수 조회
     * @return 총 금융용어 개수
     */
    int getTermsCount();
    
    /**
     * 용어로 검색 (부분 일치)
     * @param term 검색할 용어
     * @return 검색 결과 리스트
     */
    List<FinanceTermVO> searchByTerm(String term);
    
    /**
     * 주제로 검색
     * @param subject 검색할 주제
     * @return 검색 결과 리스트
     */
    List<FinanceTermVO> searchBySubject(String subject);
    
    /**
     * 용어 상세 조회 (정확한 일치)
     * @param term 조회할 용어
     * @return 금융용어 정보
     */
    FinanceTermVO getTermByExactMatch(String term);
    
    /**
     * 페이징 처리된 용어 조회
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 금융용어 리스트
     */
    List<FinanceTermVO> getTermsWithPaging(int offset, int limit);
}

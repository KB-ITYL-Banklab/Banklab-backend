package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.FinanceTermVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 금융용어 매퍼 인터페이스
 * finance_terms 테이블과 연동하는 MyBatis 매퍼
 */
@Mapper
public interface FinanceTermMapper {
    
    /**
     * 모든 금융용어 조회
     * @return 금융용어 리스트
     */
    List<FinanceTermVO> selectAllTerms();
    
    /**
     * 금융용어 개수 조회
     * @return 총 개수
     */
    int selectTermsCount();
    
    /**
     * 용어로 검색 (부분 일치) - 키워드와 제목에서만 검색
     * @param term 검색할 용어
     * @return 검색 결과 리스트
     */
    List<FinanceTermVO> searchByTerm(@Param("term") String term);
    
    /**
     * 주제로 검색 (키워드와 제목에서만 주제 키워드 검색)
     * @param subject 검색할 주제 키워드
     * @return 검색 결과 리스트
     */
    List<FinanceTermVO> searchBySubject(@Param("subject") String subject);
    
    /**
     * 정확한 용어로 조회 (키워드 또는 제목 정확 일치)
     * @param term 조회할 용어
     * @return 금융용어 정보
     */
    FinanceTermVO selectByExactTerm(@Param("term") String term);
    
    /**
     * 키워드로 정확한 조회
     * @param keyword 조회할 키워드
     * @return 금융용어 정보
     */
    FinanceTermVO selectByKeyword(@Param("keyword") String keyword);
    
    /**
     * 페이징 처리된 용어 조회
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 금융용어 리스트
     */
    List<FinanceTermVO> selectTermsWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 금융용어 삽입
     * @param financeTermVO 삽입할 금융용어
     * @return 삽입된 행 수
     */
    int insertTerm(FinanceTermVO financeTermVO);
    
    /**
     * 금융용어 업데이트
     * @param financeTermVO 업데이트할 금융용어
     * @return 업데이트된 행 수
     */
    int updateTerm(FinanceTermVO financeTermVO);
    
    /**
     * 금융용어 삭제
     * @param id 삭제할 금융용어 ID
     * @return 삭제된 행 수
     */
    int deleteTerm(@Param("id") Long id);
    
    /**
     * ID로 금융용어 조회
     * @param id 조회할 ID
     * @return 금융용어 정보
     */
    FinanceTermVO selectById(@Param("id") Long id);
    
    /**
     * 전체 데이터 삭제 (CSV 재업로드용)
     * @return 삭제된 행 수
     */
    int deleteAllTerms();
    
    /**
     * 배치 삽입
     * @param terms 삽입할 금융용어 리스트
     * @return 삽입된 행 수
     */
    int insertTermsBatch(@Param("terms") List<FinanceTermVO> terms);
}

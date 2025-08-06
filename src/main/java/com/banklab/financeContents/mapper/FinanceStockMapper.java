package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.FinanceStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 주식 정보 매퍼 인터페이스
 * finance_stock 테이블과 관련된 데이터베이스 작업을 담당
 */
@Mapper
public interface FinanceStockMapper {
    
    /**
     * 주식 정보 저장
     * @param financeStock 저장할 주식 정보
     * @return 저장된 레코드 수
     */
    int insert(FinanceStockVO financeStock);
    
    /**
     * 주식 정보 일괄 저장
     * @param stockList 저장할 주식 정보 리스트
     * @return 저장된 레코드 수
     */
    int insertBatch(List<FinanceStockVO> stockList);
    
    /**
     * 종목코드와 기준일자로 주식 정보 조회
     * @param srtnCd 종목코드
     * @param basDt 기준일자
     * @return 주식 정보
     */
    FinanceStockVO selectByCodeAndDate(@Param("srtnCd") String srtnCd, @Param("basDt") LocalDate basDt);
    
    /**
     * 기준일자로 모든 주식 정보 조회
     * @param basDt 기준일자
     * @return 주식 정보 리스트
     */
    List<FinanceStockVO> selectByDate(@Param("basDt") LocalDate basDt);
    
    /**
     * 종목코드로 최신 주식 정보 조회
     * @param srtnCd 종목코드
     * @return 최신 주식 정보
     */
    FinanceStockVO selectLatestByCode(@Param("srtnCd") String srtnCd);
    
    /**
     * 주식 정보 업데이트
     * @param financeStock 업데이트할 주식 정보
     * @return 업데이트된 레코드 수
     */
    int update(FinanceStockVO financeStock);
    
    /**
     * 중복 체크 (종목코드 + 기준일자)
     * @param srtnCd 종목코드
     * @param basDt 기준일자
     * @return 존재 여부 (1: 존재, 0: 없음)
     */
    int existsByCodeAndDate(@Param("srtnCd") String srtnCd, @Param("basDt") LocalDate basDt);
    
    /**
     * 기준일자의 데이터 삭제
     * @param basDt 기준일자
     * @return 삭제된 레코드 수
     */
    int deleteByDate(@Param("basDt") LocalDate basDt);
    
    /**
     * 전체 데이터 수 조회
     * @return 총 레코드 수
     */
    int selectTotalCount();
    
    /**
     * 최신 N개 종목 조회
     * @param limit 조회할 개수
     * @return 최신 주식 정보 리스트
     */
    List<FinanceStockVO> selectLatestStocks(@Param("limit") int limit);
    
    /**
     * 주식명으로 검색 (부분 일치)
     * @param stockName 검색할 주식명 (부분 검색 가능)
     * @return 일치하는 주식 정보 리스트
     */
    List<FinanceStockVO> selectByStockName(@Param("stockName") String stockName);
    
    /**
     * 주식명으로 최신 데이터 검색 (부분 일치)
     * @param stockName 검색할 주식명 (부분 검색 가능)
     * @param limit 조회할 개수
     * @return 일치하는 최신 주식 정보 리스트
     */
    List<FinanceStockVO> selectLatestByStockName(@Param("stockName") String stockName, @Param("limit") int limit);
    
    /**
     * 30일 이전 데이터 삭제
     * @param cutoffDate 삭제 기준 날짜
     * @return 삭제된 레코드 수
     */
    int deleteOldDataBefore(@Param("cutoffDate") LocalDate cutoffDate);
}

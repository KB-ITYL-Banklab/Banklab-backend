package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceStockVO;
import com.banklab.financeContents.dto.StockSecurityInfoDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 주식 정보 데이터베이스 서비스 인터페이스
 */
public interface FinanceStockService {
    
    /**
     * API에서 주식 정보를 가져와서 데이터베이스에 저장
     * @param baseDate 기준일자
     * @return 저장된 레코드 수
     */
    int saveStockDataFromApi(LocalDate baseDate);
    
    /**
     * API에서 상위 종목들만 선별하여 배치로 저장 (API 제한 대응)
     * @param baseDate 기준일자
     * @param topCount 상위 종목 수 (기본 200개)
     * @return 저장된 레코드 수
     */
    int saveTopStockDataFromApi(LocalDate baseDate, int topCount);
    
    /**
     * 최근 N일간의 데이터를 배치로 저장
     * @param days 최근 N일
     * @param topCount 일별 상위 종목 수
     * @return 총 저장된 레코드 수
     */
    int saveRecentStockData(int days, int topCount);
    
    /**
     * 특정 종목 정보를 API에서 가져와서 저장
     * @param shortCode 종목코드
     * @return 저장 성공 여부
     */
    boolean saveStockByCode(String shortCode);
    
    /**
     * 주식 정보 목록을 데이터베이스에 저장
     * @param stockDtoList API에서 가져온 주식 정보 DTO 리스트
     * @return 저장된 레코드 수
     */
    int saveStockList(List<StockSecurityInfoDto> stockDtoList);
    
    /**
     * 기준일자로 주식 정보 조회
     * @param baseDate 기준일자
     * @return 주식 정보 리스트
     */
    List<FinanceStockVO> getStocksByDate(LocalDate baseDate);
    
    /**
     * 종목코드로 최신 주식 정보 조회
     * @param stockCode 종목코드
     * @return 최신 주식 정보
     */
    FinanceStockVO getLatestStockByCode(String stockCode);
    
    /**
     * 최신 인기 종목 조회
     * @param limit 조회할 개수
     * @return 인기 종목 리스트
     */
    List<FinanceStockVO> getTopStocks(int limit);
    
    /**
     * 최신 날짜의 모든 주식 데이터 조회
     * @return 최신 날짜의 모든 주식 정보 리스트
     */
    List<FinanceStockVO> getLatestStocksByDate();
    
    /**
     * 주식명으로 검색 (부분 일치)
     * @param stockName 검색할 주식명
     * @return 일치하는 주식 정보 리스트 (모든 날짜)
     */
    List<FinanceStockVO> searchStocksByName(String stockName);
    
    /**
     * 주식명으로 최신 데이터만 검색 (부분 일치)
     * @param stockName 검색할 주식명
     * @param limit 조회할 개수 (기본값 적용시 null 가능)
     * @return 일치하는 최신 주식 정보 리스트
     */
    List<FinanceStockVO> searchLatestStocksByName(String stockName, Integer limit);
    
    /**
     * 주식명으로 검색 (정확한 일치, 모든 날짜)
     * @param stockName 검색할 주식명
     * @return 일치하는 주식 정보 리스트 (모든 날짜)
     */
    List<FinanceStockVO> searchStocksByExactName(String stockName);
    
    /**
     * 주식명으로 최신 데이터만 검색 (정확한 일치)
     * @param stockName 검색할 주식명
     * @param limit 조회할 개수 (기본값 적용시 null 가능)
     * @return 일치하는 최신 주식 정보 리스트
     */
    List<FinanceStockVO> searchLatestStocksByExactName(String stockName, Integer limit);
    
    /**
     * 주식 정보 업데이트
     * @param financeStock 업데이트할 주식 정보
     * @return 업데이트 성공 여부
     */
    boolean updateStock(FinanceStockVO financeStock);
    
    /**
     * 중복 데이터 체크
     * @param stockCode 종목코드
     * @param baseDate 기준일자
     * @return 존재 여부
     */
    boolean isStockExists(String stockCode, LocalDate baseDate);
    
    /**
     * 30일 이전 오래된 데이터 삭제
     * @return 삭제된 레코드 수
     */
    int deleteOldData();
}

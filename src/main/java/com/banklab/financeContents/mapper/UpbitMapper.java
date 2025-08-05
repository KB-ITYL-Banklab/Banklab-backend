package com.banklab.financeContents.mapper;

import com.banklab.financeContents.domain.FinanceUpbit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 업비트 데이터 MyBatis Mapper
 */
@Mapper
public interface UpbitMapper {
    
    /**
     * 업비트 데이터 삽입
     */
    void insertUpbitData(FinanceUpbit financeUpbit);
    
    /**
     * 업비트 데이터 일괄 삽입
     */
    void insertUpbitDataBatch(List<FinanceUpbit> financeUpbitList);
    
    /**
     * 특정 마켓 코드의 최신 데이터 조회
     */
    FinanceUpbit selectLatestByMarket(@Param("market") String market);
    
    /**
     * 모든 마켓의 최신 데이터 조회
     */
    List<FinanceUpbit> selectAllLatestData();
    
    /**
     * 특정 날짜의 데이터 존재 여부 확인
     */
    int countTodayData(@Param("market") String market, @Param("date") String date);
    
    /**
     * 기존 데이터 업데이트 (같은 날짜의 데이터가 있을 경우)
     */
    void updateUpbitData(FinanceUpbit financeUpbit);
    
    /**
     * 최근 한달치 데이터 일괄 삽입
     */
    void insertMonthlyData(List<FinanceUpbit> financeUpbitList);
    
    /**
     * 종목명으로 해당 종목의 모든 데이터 조회
     */
    List<FinanceUpbit> selectDataByMarket(@Param("market") String market);
    
    /**
     * 종목명으로 해당 종목의 특정 기간 데이터 조회
     */
    List<FinanceUpbit> selectDataByMarketAndDateRange(
        @Param("market") String market, 
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
}

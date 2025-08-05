package com.banklab.financeContents.mapper;

import com.banklab.financeContents.dto.UpbitCryptoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 업비트 가상화폐 시세 정보 관련 MyBatis Mapper 인터페이스
 */
@Mapper
public interface UpbitCryptoMapper {

    /**
     * 가상화폐 시세 정보를 데이터베이스에 삽입
     * @param upbitCrypto 삽입할 가상화폐 시세 정보
     * @return 삽입된 행의 수
     */
    int insertCrypto(UpbitCryptoDTO upbitCrypto);

    /**
     * 여러 가상화폐 시세 정보를 일괄 삽입
     * @param cryptoList 삽입할 가상화폐 시세 정보 리스트
     * @return 삽입된 행의 수
     */
    int insertCryptoList(@Param("list") List<UpbitCryptoDTO> cryptoList);

    /**
     * 특정 마켓의 최신 시세 정보 조회
     * @param market 마켓 코드 (예: KRW-BTC)
     * @return 최신 시세 정보
     */
    UpbitCryptoDTO selectLatestByMarket(@Param("market") String market);

    /**
     * 모든 가상화폐의 최신 시세 정보 조회
     * @return 최신 시세 정보 리스트
     */
    List<UpbitCryptoDTO> selectAllLatest();

    /**
     * 특정 마켓의 시세 정보 조회 (페이징)
     * @param market 마켓 코드
     * @param limit 조회할 행의 수
     * @param offset 시작 행
     * @return 시세 정보 리스트
     */
    List<UpbitCryptoDTO> selectByMarketWithPaging(@Param("market") String market, 
                                                  @Param("limit") int limit, 
                                                  @Param("offset") int offset);

    /**
     * ID로 특정 시세 정보 조회
     * @param id 고유 ID
     * @return 시세 정보
     */
    UpbitCryptoDTO selectById(@Param("id") Long id);

    /**
     * 전체 시세 정보 개수 조회
     * @return 전체 개수
     */
    int selectTotalCount();

    /**
     * 특정 마켓의 시세 정보 개수 조회
     * @param market 마켓 코드
     * @return 해당 마켓의 시세 정보 개수
     */
    int selectCountByMarket(@Param("market") String market);

    /**
     * 특정 날짜 이전의 데이터 삭제 (데이터 정리용)
     * @param days 보관할 일수
     * @return 삭제된 행의 수
     */
    int deleteOldData(@Param("days") int days);

    /**
     * 특정 날짜 범위의 데이터 조회
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param market 마켓 코드 (선택)
     * @param limit 조회할 행의 수
     * @param offset 시작 행
     * @return 시세 정보 리스트
     */
    List<UpbitCryptoDTO> selectByDateRange(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("market") String market,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    /**
     * 오늘 데이터 조회
     * @param market 마켓 코드 (선택)
     * @return 오늘의 시세 정보 리스트
     */
    List<UpbitCryptoDTO> selectTodayData(@Param("market") String market);
}

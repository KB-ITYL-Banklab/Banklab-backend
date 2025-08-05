package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.UpbitCryptoDTO;

import java.util.List;

/**
 * 업비트 가상화폐 시세 정보 관련 서비스 인터페이스
 */
public interface UpbitCryptoService {

    /**
     * 업비트 API에서 시세 정보를 가져와서 데이터베이스에 저장
     * @param markets 마켓 코드들 (쉼표로 구분, 예: "KRW-BTC,KRW-ETH")
     * @return 저장된 데이터 개수
     */
    int fetchAndSaveCryptoData(String markets);

    /**
     * 단일 가상화폐 시세 정보를 데이터베이스에 저장
     * @param upbitCrypto 저장할 시세 정보
     * @return 저장 성공 여부
     */
    boolean saveCrypto(UpbitCryptoDTO upbitCrypto);

    /**
     * 여러 가상화폐 시세 정보를 일괄 저장
     * @param cryptoList 저장할 시세 정보 리스트
     * @return 저장 성공 여부
     */
    boolean saveCryptoList(List<UpbitCryptoDTO> cryptoList);

    /**
     * 특정 마켓의 최신 시세 정보 조회
     * @param market 마켓 코드 (예: KRW-BTC)
     * @return 최신 시세 정보
     */
    UpbitCryptoDTO getLatestByMarket(String market);

    /**
     * 모든 가상화폐의 최신 시세 정보 조회
     * @return 최신 시세 정보 리스트
     */
    List<UpbitCryptoDTO> getAllLatest();

    /**
     * 특정 마켓의 시세 정보 조회 (페이징)
     * @param market 마켓 코드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 시세 정보 리스트
     */
    List<UpbitCryptoDTO> getCryptoByMarket(String market, int page, int size);

    /**
     * ID로 특정 시세 정보 조회
     * @param id 고유 ID
     * @return 시세 정보
     */
    UpbitCryptoDTO getCryptoById(Long id);

    /**
     * 전체 시세 정보 개수 조회
     * @return 전체 개수
     */
    int getTotalCount();

    /**
     * 특정 마켓의 시세 정보 개수 조회
     * @param market 마켓 코드
     * @return 해당 마켓의 시세 정보 개수
     */
    int getCountByMarket(String market);

    /**
     * 오래된 데이터 정리
     * @param daysToKeep 보관할 일수
     * @return 삭제된 데이터 개수
     */
    int cleanOldData(int daysToKeep);

    /**
     * 인기 가상화폐들의 시세를 가져와서 저장하는 배치 작업
     * @return 저장된 데이터 개수
     */
    int fetchAndSavePopularCryptos();

    /**
     * 특정 날짜 범위의 데이터 조회
     * @param startDate 시작 날짜 (YYYY-MM-DD)
     * @param endDate 종료 날짜 (YYYY-MM-DD)
     * @param market 마켓 코드 (선택)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 시세 정보 리스트
     */
    List<UpbitCryptoDTO> getCryptoByDateRange(String startDate, String endDate, String market, int page, int size);

    /**
     * 오늘 데이터 조회
     * @param market 마켓 코드 (선택, null이면 전체)
     * @return 오늘의 시세 정보 리스트
     */
    List<UpbitCryptoDTO> getTodayData(String market);
}

package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.BitcoinTickerDTO;
import com.banklab.financeContents.dto.UpbitCryptoDTO;
import com.banklab.financeContents.mapper.UpbitCryptoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 업비트 가상화폐 시세 정보 관련 서비스 구현체
 */
@Service
@Transactional
public class UpbitCryptoServiceImpl implements UpbitCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(UpbitCryptoServiceImpl.class);

    @Autowired
    private UpbitCryptoMapper upbitCryptoMapper;

    @Autowired
    private UpbitApiService upbitApiService;

    /**
     * 인기 가상화폐 마켓 코드들
     */
    private static final String POPULAR_MARKETS = "KRW-BTC,KRW-ETH,KRW-XRP,KRW-ADA,KRW-DOT,KRW-LINK,KRW-LTC,KRW-BCH,KRW-EOS,KRW-TRX";

    @Override
    public int fetchAndSaveCryptoData(String markets) {
        try {
            logger.info("업비트 API에서 가상화폐 시세 데이터 가져오기 시작: {}", markets);

            // 업비트 API에서 데이터 가져오기
            List<BitcoinTickerDTO> tickerList = upbitApiService.getMultipleTickers(markets);

            if (tickerList == null || tickerList.isEmpty()) {
                logger.warn("업비트 API에서 데이터를 가져오지 못했습니다: {}", markets);
                return 0;
            }

            // BitcoinTickerDTO를 UpbitCryptoDTO로 변환
            List<UpbitCryptoDTO> cryptoList = new ArrayList<>();
            for (BitcoinTickerDTO ticker : tickerList) {
                UpbitCryptoDTO crypto = convertToUpbitCryptoDTO(ticker);
                cryptoList.add(crypto);
            }

            // 데이터베이스에 일괄 저장
            int savedCount = upbitCryptoMapper.insertCryptoList(cryptoList);
            logger.info("{}개의 가상화폐 시세 데이터를 데이터베이스에 저장했습니다", savedCount);

            return savedCount;

        } catch (Exception e) {
            logger.error("가상화폐 시세 데이터 가져오기 및 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("가상화폐 시세 데이터 처리 실패", e);
        }
    }

    @Override
    public boolean saveCrypto(UpbitCryptoDTO upbitCrypto) {
        try {
            int result = upbitCryptoMapper.insertCrypto(upbitCrypto);
            logger.debug("가상화폐 시세 정보 저장 완료: {}", upbitCrypto.getMarket());
            return result > 0;
        } catch (Exception e) {
            logger.error("가상화폐 시세 정보 저장 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean saveCryptoList(List<UpbitCryptoDTO> cryptoList) {
        try {
            if (cryptoList == null || cryptoList.isEmpty()) {
                logger.warn("저장할 가상화폐 시세 데이터가 없습니다");
                return false;
            }

            int result = upbitCryptoMapper.insertCryptoList(cryptoList);
            logger.info("{}개의 가상화폐 시세 정보를 일괄 저장했습니다", cryptoList.size());
            return result > 0;
        } catch (Exception e) {
            logger.error("가상화폐 시세 정보 일괄 저장 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UpbitCryptoDTO getLatestByMarket(String market) {
        try {
            UpbitCryptoDTO result = upbitCryptoMapper.selectLatestByMarket(market);
            logger.debug("{}의 최신 시세 정보 조회 완료", market);
            return result;
        } catch (Exception e) {
            logger.error("최신 시세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpbitCryptoDTO> getAllLatest() {
        try {
            List<UpbitCryptoDTO> result = upbitCryptoMapper.selectAllLatest();
            logger.debug("모든 가상화폐의 최신 시세 정보 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("모든 최신 시세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpbitCryptoDTO> getCryptoByMarket(String market, int page, int size) {
        try {
            int offset = page * size;
            List<UpbitCryptoDTO> result = upbitCryptoMapper.selectByMarketWithPaging(market, size, offset);
            logger.debug("{}의 시세 정보 페이징 조회 완료: {}페이지, {}개", market, page, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("시세 정보 페이징 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UpbitCryptoDTO getCryptoById(Long id) {
        try {
            UpbitCryptoDTO result = upbitCryptoMapper.selectById(id);
            logger.debug("ID {}의 시세 정보 조회 완료", id);
            return result;
        } catch (Exception e) {
            logger.error("ID로 시세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalCount() {
        try {
            int count = upbitCryptoMapper.selectTotalCount();
            logger.debug("전체 시세 정보 개수 조회 완료: {}개", count);
            return count;
        } catch (Exception e) {
            logger.error("전체 개수 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getCountByMarket(String market) {
        try {
            int count = upbitCryptoMapper.selectCountByMarket(market);
            logger.debug("{}의 시세 정보 개수 조회 완료: {}개", market, count);
            return count;
        } catch (Exception e) {
            logger.error("마켓별 개수 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int cleanOldData(int daysToKeep) {
        try {
            int deletedCount = upbitCryptoMapper.deleteOldData(daysToKeep);
            logger.info("{}일 이전의 오래된 데이터 {}개를 삭제했습니다", daysToKeep, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("오래된 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int fetchAndSavePopularCryptos() {
        logger.info("인기 가상화폐들의 시세 데이터 가져오기 시작");
        return fetchAndSaveCryptoData(POPULAR_MARKETS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpbitCryptoDTO> getCryptoByDateRange(String startDate, String endDate, String market, int page, int size) {
        try {
            int offset = page * size;
            List<UpbitCryptoDTO> result = upbitCryptoMapper.selectByDateRange(startDate, endDate, market, size, offset);
            logger.debug("날짜 범위 조회 완료: {} ~ {}, 마켓: {}, {}페이지, {}개", 
                        startDate, endDate, market, page, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("날짜 범위 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpbitCryptoDTO> getTodayData(String market) {
        try {
            List<UpbitCryptoDTO> result = upbitCryptoMapper.selectTodayData(market);
            logger.debug("오늘 데이터 조회 완료: 마켓 {}, {}개", market, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("오늘 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * BitcoinTickerDTO를 UpbitCryptoDTO로 변환
     */
    private UpbitCryptoDTO convertToUpbitCryptoDTO(BitcoinTickerDTO ticker) {
        UpbitCryptoDTO crypto = new UpbitCryptoDTO();
        
        crypto.setMarket(ticker.getMarket());
        crypto.setOpeningPrice(ticker.getOpeningPrice());
        crypto.setTradePrice(ticker.getTradePrice());
        crypto.setPrevClosingPrice(ticker.getPrevClosingPrice());
        crypto.setChangeRate(ticker.getSignedChangeRate());
        crypto.setAccTradeVolume24h(ticker.getAccTradeVolume24h());
        crypto.setAccTradePrice24h(ticker.getAccTradePrice24h());
        
        return crypto;
    }
}

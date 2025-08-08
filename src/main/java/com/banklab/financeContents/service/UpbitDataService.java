package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceUpbit;
import com.banklab.financeContents.dto.UpbitTickerDto;
import com.banklab.financeContents.mapper.UpbitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 업비트 데이터 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitDataService {

    private final UpbitMapper upbitMapper;
    private final UpbitApiService upbitApiService;

    /**
     * 업비트 데이터 수집 및 저장
     */
    @Transactional
    public void collectAndSaveUpbitData() {
        try {
            log.info("=== 업비트 데이터 수집 시작 ===");
            
            // API에서 데이터 조회
            log.info("업비트 API 호출 시작");
            List<UpbitTickerDto> tickers = upbitApiService.getAllKrwTickers();
            log.info("업비트 API 호출 완료. 조회된 마켓 수: {}", tickers.size());
            
            if (tickers.isEmpty()) {
                log.warn("조회된 업비트 데이터가 없습니다. API 호출 실패 가능성");
                throw new RuntimeException("업비트 API에서 데이터를 가져올 수 없습니다.");
            }

            // 첫 번째 데이터 샘플 로깅
            if (!tickers.isEmpty()) {
                UpbitTickerDto sample = tickers.get(0);
                log.info("샘플 데이터: {} - 현재가: {}, 등락률: {}", 
                    sample.getMarket(), sample.getTrade_price(), sample.getChange_rate());
            }

            // DTO를 Domain으로 변환
            log.info("데이터 변환 시작");
            List<FinanceUpbit> financeUpbitList = tickers.stream()
                .map(this::convertToFinanceUpbit)
                .toList();
            log.info("데이터 변환 완료. 변환된 데이터 수: {}", financeUpbitList.size());

            // 데이터 저장 전략: 오늘 날짜에 해당하는 데이터가 있으면 업데이트, 없으면 삽입
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            log.info("오늘 날짜: {}", today);
            
            int insertCount = 0;
            int updateCount = 0;
            
            for (FinanceUpbit financeUpbit : financeUpbitList) {
                try {
                    int count = upbitMapper.countTodayData(financeUpbit.getMarket(), today);
                    log.debug("마켓 {} 오늘 데이터 존재 여부: {}", financeUpbit.getMarket(), count);
                    
                    if (count > 0) {
                        // 오늘 데이터가 이미 있으면 업데이트
                        upbitMapper.updateUpbitData(financeUpbit);
                        updateCount++;
                        log.debug("업데이트 완료: {} - 현재가: {}", 
                            financeUpbit.getMarket(), financeUpbit.getTradePrice());
                    } else {
                        // 오늘 데이터가 없으면 삽입
                        upbitMapper.insertUpbitData(financeUpbit);
                        insertCount++;
                        log.debug("삽입 완료: {} - 현재가: {}", 
                            financeUpbit.getMarket(), financeUpbit.getTradePrice());
                    }
                } catch (Exception e) {
                    log.error("마켓 {} 데이터 저장 실패", financeUpbit.getMarket(), e);
                    throw e; // 트랜잭션 롤백을 위해 예외 재발생
                }
            }

            log.info("=== 업비트 데이터 수집 완료 ===");
            log.info("처리 결과 - 삽입: {}건, 업데이트: {}건, 전체: {}건", 
                insertCount, updateCount, financeUpbitList.size());
            
        } catch (Exception e) {
            log.error("=== 업비트 데이터 수집 실패 ===", e);
            throw new RuntimeException("업비트 데이터 수집 실패: " + e.getMessage(), e);
        }
    }

    /**
     * UpbitTickerDto를 FinanceUpbit으로 변환
     */
    private FinanceUpbit convertToFinanceUpbit(UpbitTickerDto ticker) {
        FinanceUpbit financeUpbit = new FinanceUpbit();
        
        financeUpbit.setMarket(ticker.getMarket());
        
        // 가격 필드를 BigDecimal로 변환하고 소수점 둘째 자리로 반올림
        financeUpbit.setOpeningPrice(ticker.getOpening_price() != null ? 
            java.math.BigDecimal.valueOf(ticker.getOpening_price())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setTradePrice(ticker.getTrade_price() != null ? 
            java.math.BigDecimal.valueOf(ticker.getTrade_price())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setPrevClosingPrice(ticker.getPrev_closing_price() != null ? 
            java.math.BigDecimal.valueOf(ticker.getPrev_closing_price())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        
        // 등락률은 Double 유지
        financeUpbit.setChangeRate(ticker.getChange_rate());
        
        // 거래량, 거래대금은 Double 유지 (DOUBLE 컬럼들)
        financeUpbit.setAccTradeVolume24h(ticker.getAcc_trade_volume_24h());
        financeUpbit.setAccTradePrice24h(ticker.getAcc_trade_price_24h());
        
        log.debug("변환된 데이터: {} - 시가: {}, 현재가: {}, 등락률: {}", 
            financeUpbit.getMarket(), financeUpbit.getOpeningPrice(), 
            financeUpbit.getTradePrice(), financeUpbit.getChangeRate());
        
        return financeUpbit;
    }

    /**
     * 특정 마켓의 최신 데이터 조회
     */
    public FinanceUpbit getLatestDataByMarket(String market) {
        log.info("마켓 {} 최신 데이터 조회", market);
        FinanceUpbit result = upbitMapper.selectLatestByMarket(market);
        log.info("마켓 {} 조회 결과: {}", market, result != null ? "데이터 존재" : "데이터 없음");
        return result;
    }

    /**
     * 모든 마켓의 최신 데이터 조회
     */
    public List<FinanceUpbit> getAllLatestData() {
        log.info("전체 마켓 최신 데이터 조회");
        List<FinanceUpbit> result = upbitMapper.selectAllLatestData();
        log.info("전체 마켓 조회 결과: {}건", result.size());
        return result;
    }

    /**
     * 테스트용: API 연결 상태 확인
     */
    public String testApiConnection() {
        try {
            log.info("=== API 연결 테스트 시작 (주입된 서비스 사용) ===");
            
            // 마켓 조회 테스트
            List<com.banklab.financeContents.dto.UpbitMarketDto> markets = upbitApiService.getAllMarkets();
            log.info("마켓 조회 결과: {}개", markets.size());
            
            if (markets.isEmpty()) {
                return "마켓 조회 실패 - 0개";
            }
            
            // 상위 5개 마켓으로 Ticker 조회 테스트
            List<String> testMarkets = markets.stream()
                .limit(5)
                .map(com.banklab.financeContents.dto.UpbitMarketDto::getMarket)
                .toList();
            
            log.info("테스트 마켓들: {}", testMarkets);
            
            List<UpbitTickerDto> tickers = upbitApiService.getTickers(testMarkets);
            log.info("Ticker 조회 결과: {}개", tickers.size());
            
            if (tickers.isEmpty()) {
                return String.format("마켓은 %d개 조회되었으나 Ticker 데이터는 0개", markets.size());
            }
            
            UpbitTickerDto sample = tickers.get(0);
            return String.format("성공! 마켓: %d개, Ticker: %d개, 샘플: %s(현재가: %,.0f원)", 
                markets.size(), tickers.size(), sample.getMarket(), sample.getTrade_price());
                
        } catch (Exception e) {
            log.error("API 연결 테스트 실패", e);
            return "API 연결 테스트 실패: " + e.getMessage();
        }
    }

    /**
     * 테스트용: 단일 마켓 Ticker 조회
     */
    public String testSingleTicker() {
        try {
            log.info("=== 단일 마켓 Ticker 테스트 시작 ===");
            
            // BTC 하나만 테스트
            List<String> singleMarket = List.of("KRW-BTC");
            log.info("테스트 마켓: {}", singleMarket);
            
            List<UpbitTickerDto> tickers = upbitApiService.getTickers(singleMarket);
            log.info("단일 마켓 Ticker 조회 결과: {}개", tickers.size());
            
            if (tickers.isEmpty()) {
                return "단일 마켓(BTC) Ticker 조회 실패 - 0개";
            }
            
            UpbitTickerDto btc = tickers.get(0);
            return String.format("단일 마켓 성공! %s - 현재가: %,.0f원, 등락률: %.2f%%", 
                btc.getMarket(), btc.getTrade_price(), 
                btc.getChange_rate() != null ? btc.getChange_rate() * 100 : 0.0);
                
        } catch (Exception e) {
            log.error("단일 마켓 Ticker 테스트 실패", e);
            return "단일 마켓 Ticker 테스트 실패: " + e.getMessage();
        }
    }

    /**
     * 테스트용: 배치 처리 테스트
     */
    public String testBatchProcessing() {
        try {
            log.info("=== 배치 처리 테스트 시작 ===");
            
            List<UpbitTickerDto> tickers = upbitApiService.getAllKrwTickers();
            
            if (tickers.isEmpty()) {
                return "배치 처리 실패 - 수집된 Ticker 0개";
            }
            
            // 샘플 데이터 확인
            UpbitTickerDto sample = tickers.get(0);
            
            return String.format("배치 처리 성공! 총 %d개 Ticker 수집, 샘플: %s(현재가: %,.0f원)", 
                tickers.size(), sample.getMarket(), sample.getTrade_price());
                
        } catch (Exception e) {
            log.error("배치 처리 테스트 실패", e);
            return "배치 처리 테스트 실패: " + e.getMessage();
        }
    }

    /**
     * 최근 한달치 실제 과거 데이터를 DB에 삽입
     * 업비트 API의 일봉 캔들 데이터를 사용하여 실제 과거 한달치 데이터를 수집합니다.
     */
    @Transactional
    public void insertMonthlyData() {
        try {
            log.info("=== 최근 한달치 실제 데이터 삽입 시작 ===");
            
            // 업비트 API에서 모든 KRW 마켓의 한달치 일봉 데이터를 가져옵니다
            List<com.banklab.financeContents.dto.UpbitCandleDto> candleDataList = upbitApiService.getAllMarketsMonthlyCandles();
            
            if (candleDataList.isEmpty()) {
                throw new RuntimeException("업비트 API에서 캔들 데이터를 가져올 수 없습니다.");
            }
            
            log.info("업비트 API에서 {}개의 캔들 데이터를 수집했습니다.", candleDataList.size());
            
            // 캔들 데이터를 FinanceUpbit 엔티티로 변환
            List<FinanceUpbit> financeUpbitList = candleDataList.stream()
                .map(this::convertCandleToFinanceUpbit)
                .toList();
            
            log.info("{}개의 캔들 데이터를 FinanceUpbit 엔티티로 변환 완료", financeUpbitList.size());
            
            // 배치 삽입
            if (!financeUpbitList.isEmpty()) {
                upbitMapper.insertMonthlyData(financeUpbitList);
                log.info("최근 한달치 실제 데이터 삽입 완료: {}건", financeUpbitList.size());
            }
            
        } catch (Exception e) {
            log.error("최근 한달치 데이터 삽입 실패", e);
            throw new RuntimeException("최근 한달치 데이터 삽입 실패: " + e.getMessage(), e);
        }
    }

    /**
     * UpbitCandleDto를 FinanceUpbit으로 변환 (candleDateTime 포함)
     */
    private FinanceUpbit convertCandleToFinanceUpbit(com.banklab.financeContents.dto.UpbitCandleDto candle) {
        FinanceUpbit financeUpbit = new FinanceUpbit();
        
        financeUpbit.setMarket(candle.getMarket());
        
        // 캔들 날짜/시간 설정 (Date 타입으로 변환)
        if (candle.getCandleDateTimeKst() != null) {
            try {
                // "2025-07-15T09:00:00" 형식의 문자열을 Date로 변환
                String dateTimeStr = candle.getCandleDateTimeKst().replace("+09:00", "");
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(dateTimeStr);
                
                // LocalDateTime을 Date로 변환 (KST 기준)
                java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Seoul");
                java.time.ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
                java.util.Date candleDate = java.util.Date.from(zonedDateTime.toInstant());
                
                financeUpbit.setCandleDateTime(candleDate);
                
                log.debug("캔들 날짜 변환 성공: {} -> {}", candle.getCandleDateTimeKst(), candleDate);
                
            } catch (Exception e) {
                log.warn("캔들 날짜 파싱 실패: {} - {}", candle.getMarket(), candle.getCandleDateTimeKst(), e);
                // 파싱 실패 시 현재 시간 사용
                financeUpbit.setCandleDateTime(new java.util.Date());
            }
        } else {
            // 캔들 날짜가 없으면 현재 시간 사용
            financeUpbit.setCandleDateTime(new java.util.Date());
        }
        
        // 캔들 데이터를 BigDecimal로 변환하고 소수점 둘째 자리로 반올림
        financeUpbit.setOpeningPrice(candle.getOpeningPrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getOpeningPrice())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setTradePrice(candle.getTradePrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getTradePrice())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setPrevClosingPrice(candle.getPrevClosingPrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getPrevClosingPrice())
                .setScale(2, java.math.RoundingMode.HALF_UP) : null);
        
        // 등락률은 Double 유지
        financeUpbit.setChangeRate(candle.getChangeRate());
        
        // 거래량, 거래대금
        financeUpbit.setAccTradeVolume24h(candle.getCandleAccTradeVolume());
        financeUpbit.setAccTradePrice24h(candle.getCandleAccTradePrice());
        
        log.debug("캔들 데이터 변환 완료: {} - 캔들날짜: {}, 시가: {}, 종가: {}, 등락률: {}", 
            financeUpbit.getMarket(), financeUpbit.getCandleDateTime(), 
            financeUpbit.getOpeningPrice(), financeUpbit.getTradePrice(), financeUpbit.getChangeRate());
        
        return financeUpbit;
    }

    /**
     * 종목명(마켓코드)으로 해당 종목의 모든 데이터 조회
     */
    public List<FinanceUpbit> getDataByMarket(String market) {
        log.info("종목 {} 전체 데이터 조회", market);
        List<FinanceUpbit> result = upbitMapper.selectDataByMarket(market);
        log.info("종목 {} 조회 결과: {}건", market, result.size());
        return result;
    }

    /**
     * 종목명(마켓코드)으로 해당 종목의 특정 기간 데이터 조회
     */
    public List<FinanceUpbit> getDataByMarketAndDateRange(String market, String startDate, String endDate) {
        log.info("종목 {} 기간별 데이터 조회: {} ~ {}", market, startDate, endDate);
        List<FinanceUpbit> result = upbitMapper.selectDataByMarketAndDateRange(market, startDate, endDate);
        log.info("종목 {} 기간별 조회 결과: {}건", market, result.size());
        return result;
    }

    /**
     * 실시간 1분봉 데이터 조회 (단일 종목) - Candle API 사용
     * @param market 마켓 코드
     * @return 실시간 캔들 데이터
     */
    public FinanceUpbit getRealtimeCandle(String market) {
        log.info("실시간 캔들 데이터 조회: {}", market);
        
        try {
            // 1분봉 최신 1개 조회
            com.banklab.financeContents.dto.UpbitCandleDto candleDto = upbitApiService.getLatestMinuteCandle(market);
            
            if (candleDto == null) {
                log.warn("마켓 {} 실시간 캔들 데이터 없음", market);
                return null;
            }
            
            // UpbitCandleDto를 FinanceUpbit으로 변환 (간소화된 버전)
            FinanceUpbit realtimeData = convertCandleToRealtimeData(candleDto);
            
            log.info("실시간 캔들 데이터 조회 성공: {} - 시간: {}, 현재가: {}", 
                market, realtimeData.getCandleDateTime(), realtimeData.getTradePrice());
            
            return realtimeData;
            
        } catch (Exception e) {
            log.error("실시간 캔들 데이터 조회 실패: {}", market, e);
            return null;
        }
    }

    /**
     * UpbitCandleDto를 실시간 데이터용 FinanceUpbit으로 변환 (간소화)
     */
    private FinanceUpbit convertCandleToRealtimeData(com.banklab.financeContents.dto.UpbitCandleDto candle) {
        FinanceUpbit financeUpbit = new FinanceUpbit();
        
        financeUpbit.setMarket(candle.getMarket());
        
        // 캔들 날짜/시간 설정
        if (candle.getCandleDateTimeKst() != null) {
            try {
                String dateTimeStr = candle.getCandleDateTimeKst().replace("+09:00", "");
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(dateTimeStr);
                
                java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Seoul");
                java.time.ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
                java.util.Date candleDate = java.util.Date.from(zonedDateTime.toInstant());
                
                financeUpbit.setCandleDateTime(candleDate);
            } catch (Exception e) {
                log.warn("실시간 캔들 날짜 파싱 실패: {}", candle.getMarket(), e);
                financeUpbit.setCandleDateTime(new java.util.Date());
            }
        } else {
            financeUpbit.setCandleDateTime(new java.util.Date());
        }
        
        // 가격 데이터 설정
        financeUpbit.setOpeningPrice(candle.getOpeningPrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getOpeningPrice()).setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setTradePrice(candle.getTradePrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getTradePrice()).setScale(2, java.math.RoundingMode.HALF_UP) : null);
        financeUpbit.setPrevClosingPrice(candle.getPrevClosingPrice() != null ? 
            java.math.BigDecimal.valueOf(candle.getPrevClosingPrice()).setScale(2, java.math.RoundingMode.HALF_UP) : null);
        
        // 등락률 설정
        financeUpbit.setChangeRate(candle.getChangeRate());
        
        // 거래량, 거래대금 설정
        financeUpbit.setAccTradeVolume24h(candle.getCandleAccTradeVolume());
        financeUpbit.setAccTradePrice24h(candle.getCandleAccTradePrice());
        
        log.debug("실시간 캔들 데이터 변환 완료: {} - 시간: {}, 시가: {}, 종가: {}", 
            financeUpbit.getMarket(), financeUpbit.getCandleDateTime(), 
            financeUpbit.getOpeningPrice(), financeUpbit.getTradePrice());
        
        return financeUpbit;
    }

    /**
     * 모든 마켓의 실시간 1분봉 데이터 조회
     * @return 모든 마켓의 실시간 캔들 데이터
     */
    public List<FinanceUpbit> getAllRealtimeCandles() {
        log.info("=== 모든 마켓 실시간 캔들 데이터 조회 시작 ===");
        
        try {
            List<com.banklab.financeContents.dto.UpbitCandleDto> candleDataList = 
                upbitApiService.getAllMarketsLatestCandles();
            
            if (candleDataList.isEmpty()) {
                log.warn("실시간 캔들 데이터가 없습니다");
                return List.of();
            }
            
            log.info("업비트 API에서 {}개의 실시간 캔들 데이터를 수집했습니다.", candleDataList.size());
            
            // 캔들 데이터를 FinanceUpbit 엔티티로 변환
            List<FinanceUpbit> realtimeDataList = candleDataList.stream()
                .map(this::convertCandleToFinanceUpbit)
                .toList();
            
            log.info("=== 모든 마켓 실시간 캔들 데이터 조회 완료: {}건 ===", realtimeDataList.size());
            
            return realtimeDataList;
            
        } catch (Exception e) {
            log.error("모든 마켓 실시간 데이터 조회 실패", e);
            return List.of();
        }
    }
}

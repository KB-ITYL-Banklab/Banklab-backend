package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceStockVO;
import com.banklab.financeContents.dto.StockSecurityInfoDto;
import com.banklab.financeContents.mapper.FinanceStockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 주식 정보 데이터베이스 서비스 구현체
 */
@Service
public class FinanceStockServiceImpl implements FinanceStockService {

    private static final Logger log = LoggerFactory.getLogger(FinanceStockServiceImpl.class);

    @Autowired
    private FinanceStockMapper financeStockMapper;

    @Autowired
    private PublicDataStockService publicDataStockService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public int saveStockDataFromApi(LocalDate baseDate) {
        // 기존 메서드는 상위 200개 종목만 저장하도록 변경
        return saveTopStockDataFromApi(baseDate, 200);
    }

    @Override
    @Transactional
    public int saveTopStockDataFromApi(LocalDate baseDate, int topCount) {
        try {
            log.info("🔄 {}일자 상위 {}개 종목 데이터 API 조회 및 저장 시작", baseDate, topCount);

            String baseDateStr = baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 기존 해당 날짜 데이터 삭제 (중복 방지)
            int deletedCount = financeStockMapper.deleteByDate(baseDate);
            if (deletedCount > 0) {
                log.info("🗑️ 기존 {}일자 데이터 {}건 삭제", baseDate, deletedCount);
            }

            // API 제한을 고려한 배치 처리
            List<StockSecurityInfoDto> allStocks = new ArrayList<>();
            int batchSize = 100; // 한 번에 100개씩 조회
            int currentPage = 1;
            int maxPages = (topCount / batchSize) + 1; // 상위 종목 수에 맞춰 페이지 계산

            log.info("📊 API 배치 조회 시작: {}개씩 최대 {}페이지", batchSize, maxPages);

            for (int page = currentPage; page <= maxPages && allStocks.size() < topCount; page++) {
                try {
                    log.debug("📄 {}페이지 조회 중... (목표: {}개)", page, topCount);

                    List<StockSecurityInfoDto> stockPage = publicDataStockService.getStockPriceInfo(
                            baseDateStr, null, batchSize, page);

                    if (stockPage != null && !stockPage.isEmpty()) {
                        // 한국 주식만 필터링하고 거래대금 기준으로 정렬
                        List<StockSecurityInfoDto> koreanStocks = stockPage.stream()
                                .filter(stock -> stock.getIsinCode() != null && stock.getIsinCode().startsWith("KR"))
                                .filter(stock -> stock.getTradingPrice() != null && !stock.getTradingPrice().trim().isEmpty())
                                .sorted((s1, s2) -> {
                                    try {
                                        Long price1 = parseLongValue(s1.getTradingPrice());
                                        Long price2 = parseLongValue(s2.getTradingPrice());
                                        if (price1 == null) price1 = 0L;
                                        if (price2 == null) price2 = 0L;
                                        return price2.compareTo(price1); // 내림차순
                                    } catch (Exception e) {
                                        return 0;
                                    }
                                })
                                .collect(Collectors.toList());

                        allStocks.addAll(koreanStocks);

                        log.debug("✅ {}페이지 조회 완료: {}건 (한국주식: {}건, 누적: {}건)",
                                page, stockPage.size(), koreanStocks.size(), allStocks.size());

                        // API 호출 간격 조절 (과도한 요청 방지)
                        Thread.sleep(1000); // 1초 대기

                        if (stockPage.size() < batchSize) {
                            log.debug("📄 마지막 페이지 도달");
                            break;
                        }
                    } else {
                        log.debug("📄 {}페이지에서 더 이상 데이터 없음", page);
                        break;
                    }

                } catch (Exception e) {
                    log.warn("⚠️ {}페이지 조회 실패: {}", page, e.getMessage());
                    // 한 페이지 실패해도 계속 진행
                }
            }

            if (allStocks.isEmpty()) {
                log.warn("⚠️ {}일자 API에서 조회된 한국 주식 데이터가 없습니다", baseDate);
                return 0;
            }

            // 상위 N개만 선택
            List<StockSecurityInfoDto> topStocks = allStocks.stream()
                    .limit(topCount)
                    .collect(Collectors.toList());

            int savedCount = saveStockList(topStocks);
            log.info("✅ {}일자 상위 {}개 종목 데이터 저장 완료: {}건", baseDate, topCount, savedCount);

            return savedCount;

        } catch (Exception e) {
            log.error("❌ {}일자 주식 데이터 저장 실패: {}", baseDate, e.getMessage(), e);
            throw new RuntimeException("주식 데이터 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int saveRecentStockData(int days, int topCount) {
        try {
            log.info("🗓️ 최근 {}일간 상위 {}개 종목 데이터 저장 시작", days, topCount);

            int totalSaved = 0;
            LocalDate endDate = LocalDate.now().minusDays(1); // 어제부터
            LocalDate startDate = endDate.minusDays(days); // N일 전까지

            log.info("📅 저장 기간: {} ~ {}", startDate, endDate);

            // 최근 N일간 반복
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // 주말은 건너뛰기
                if (date.getDayOfWeek().getValue() >= 6) {
                    log.info("📅 {} 주말이므로 건너뛰기", date);
                    continue;
                }

                try {
                    log.info("📊 {} 데이터 저장 시작", date);
                    int dailySaved = saveTopStockDataFromApi(date, topCount);
                    totalSaved += dailySaved;

                    log.info("✅ {} 데이터 저장 완료: {}건", date, dailySaved);

                    // 일별 저장 간격 조절 (API 제한 고려)
                    Thread.sleep(1000); // 1초 대기

                } catch (Exception e) {
                    log.error("❌ {} 데이터 저장 실패: {}", date, e.getMessage());
                    // 한 날짜 실패해도 계속 진행
                }
            }

            log.info("🎉 최근 {}일간 데이터 저장 완료: 총 {}건", days, totalSaved);
            return totalSaved;

        } catch (Exception e) {
            log.error("❌ 최근 데이터 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("최근 데이터 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int deleteOldData() {
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(30); // 30일 이전
            int deletedCount = financeStockMapper.deleteOldDataBefore(cutoffDate);
            log.info("🗑️ {} 이전 오래된 데이터 {}건 삭제", cutoffDate, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("❌ 오래된 데이터 삭제 실패: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean saveStockByCode(String shortCode) {
        try {
            log.info("🔍 종목 {} API 조회 및 저장 시작", shortCode);

            StockSecurityInfoDto stockDto = publicDataStockService.getStockInfoByCode(shortCode);

            if (stockDto == null) {
                log.warn("⚠️ 종목 {} API에서 조회되지 않음", shortCode);
                return false;
            }

            FinanceStockVO stockVO = convertDtoToVo(stockDto);

            LocalDate baseDate = stockVO.getBasDt();
            if (isStockExists(shortCode, baseDate)) {
                log.info("🔄 종목 {} {}일자 데이터 이미 존재, 업데이트 실행", shortCode, baseDate);
                return updateStock(stockVO);
            } else {
                int result = financeStockMapper.insert(stockVO);
                log.info("✅ 종목 {} {}일자 데이터 저장 완료", shortCode, baseDate);
                return result > 0;
            }

        } catch (Exception e) {
            log.error("❌ 종목 {} 저장 실패: {}", shortCode, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean saveStockByCodeAndDate(String shortCode, LocalDate baseDate) {
        try {
            log.info("🔍 종목 {} {}일자 API 조회 및 저장 시작", shortCode, baseDate);

            // 미래 날짜 체크
            if (baseDate.isAfter(LocalDate.now())) {
                log.warn("⚠️ 미래 날짜 요청: {} - 주식 데이터는 과거 날짜만 존재합니다", baseDate);
                return false;
            }

            String dateStr = baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // API srtnCd 파라미터가 필터링되지 않으므로 전체 데이터를 받아서 클라이언트에서 필터링
            log.info("🔍 전체 종목 데이터 조회 후 클라이언트 필터링 방식으로 변경");
            List<StockSecurityInfoDto> allStockDtos = publicDataStockService.getStockPriceInfo(dateStr, null, 5000, 1);

            if (allStockDtos == null || allStockDtos.isEmpty()) {
                log.warn("⚠️ {}일자 API에서 전체 데이터가 조회되지 않음", baseDate);
                return false;
            }

            log.info("📊 전체 {}개 종목 데이터에서 종목코드 {} 검색", allStockDtos.size(), shortCode);

            // 클라이언트에서 종목코드 필터링 (원본 및 0 제거 버전 모두 확인)
            String codeWithoutZero = shortCode.replaceFirst("^0+", "");
            StockSecurityInfoDto stockDto = null;

            for (StockSecurityInfoDto dto : allStockDtos) {
                if (shortCode.equals(dto.getShortCode()) || codeWithoutZero.equals(dto.getShortCode())) {
                    stockDto = dto;
                    log.info("✅ 종목코드 {} 매칭 성공: {}", shortCode, dto.getItemName());
                    break;
                }
            }

            if (stockDto == null) {
                log.warn("❌ 종목코드 {}를 전체 {}개 데이터에서 찾을 수 없습니다.", shortCode, allStockDtos.size());
                log.warn("💡 해당 날짜({})에 거래되지 않았거나 존재하지 않는 종목일 가능성이 있습니다.", baseDate);
                return false;
            }

            // 종목코드를 요청한 형식으로 통일
            if (!shortCode.equals(stockDto.getShortCode())) {
                log.info("🔧 종목코드 {} -> {}로 정규화", stockDto.getShortCode(), shortCode);
                stockDto.setShortCode(shortCode);
            }

            FinanceStockVO stockVO = convertDtoToVo(stockDto);

            if (isStockExists(shortCode, baseDate)) {
                log.info("🔄 종목 {} {}일자 데이터 이미 존재, 업데이트 실행", shortCode, baseDate);
                return updateStock(stockVO);
            } else {
                int result = financeStockMapper.insert(stockVO);
                log.info("✅ 종목 {} {}일자 데이터 저장 완료", shortCode, baseDate);
                return result > 0;
            }

        } catch (Exception e) {
            log.error("❌ 종목 {} {}일자 저장 실패: {}", shortCode, baseDate, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public int saveRecentStockDataByCode(String shortCode, int days) {
        try {
            log.info("🔍 종목 {} 최근 {}일간 데이터 저장 시작", shortCode, days);

            int savedCount = 0;
            // 오늘을 기준으로 최근 30일간 데이터 조회
            LocalDate baseDate = LocalDate.now(); // 현재 날짜 기준

            for (int i = 1; i <= days; i++) {
                LocalDate targetDate = baseDate.minusDays(i);

                // 주말 및 공휴일은 거래가 없으므로 평일만 처리
                if (targetDate.getDayOfWeek().getValue() >= 6) { // 토요일(6), 일요일(7)
                    continue;
                }

                try {
                    // 해당 날짜의 데이터가 이미 존재하는지 확인
                    if (!isStockExists(shortCode, targetDate)) {
                        boolean saved = saveStockByCodeAndDate(shortCode, targetDate);
                        if (saved) {
                            savedCount++;
                        }
                    } else {
                        log.debug("📅 종목 {} {}일자 데이터 이미 존재", shortCode, targetDate);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 종목 {} {}일자 데이터 저장 실패: {}", shortCode, targetDate, e.getMessage());
                }
            }

            log.info("✅ 종목 {} 최근 {}일간 데이터 저장 완료: {}건", shortCode, days, savedCount);
            return savedCount;

        } catch (Exception e) {
            log.error("❌ 종목 {} 최근 {}일간 데이터 저장 실패: {}", shortCode, days, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int saveStockList(List<StockSecurityInfoDto> stockDtoList) {
        if (stockDtoList == null || stockDtoList.isEmpty()) {
            log.warn("⚠️ 저장할 주식 데이터가 없습니다");
            return 0;
        }

        try {
            List<FinanceStockVO> stockVOList = new ArrayList<>();

            for (StockSecurityInfoDto dto : stockDtoList) {
                try {
                    FinanceStockVO vo = convertDtoToVo(dto);
                    stockVOList.add(vo);
                } catch (Exception e) {
                    log.warn("⚠️ DTO 변환 실패 (종목: {}): {}", dto.getShortCode(), e.getMessage());
                }
            }

            if (stockVOList.isEmpty()) {
                log.warn("⚠️ 변환된 주식 데이터가 없습니다");
                return 0;
            }

            // 배치 크기 제한 (1000건씩 나누어 저장)
            int batchSize = 1000;
            int totalSaved = 0;

            for (int i = 0; i < stockVOList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, stockVOList.size());
                List<FinanceStockVO> batch = stockVOList.subList(i, endIndex);

                try {
                    int batchResult = financeStockMapper.insertBatch(batch);
                    totalSaved += batchResult;
                    log.info("📦 배치 저장 완료: {}-{} ({}건)", i + 1, endIndex, batchResult);
                } catch (Exception e) {
                    log.error("❌ 배치 저장 실패 {}-{}: {}", i + 1, endIndex, e.getMessage());
                }
            }

            log.info("✅ 전체 주식 데이터 저장 완료: {}/{}건", totalSaved, stockDtoList.size());
            return totalSaved;

        } catch (Exception e) {
            log.error("❌ 주식 데이터 목록 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("주식 데이터 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FinanceStockVO> getStocksByDate(LocalDate baseDate) {
        try {
            return financeStockMapper.selectByDate(baseDate);
        } catch (Exception e) {
            log.error("❌ {}일자 주식 데이터 조회 실패: {}", baseDate, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public FinanceStockVO getLatestStockByCode(String stockCode) {
        try {
            return financeStockMapper.selectLatestByCode(stockCode);
        } catch (Exception e) {
            log.error("❌ 종목 {} 최신 데이터 조회 실패: {}", stockCode, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<FinanceStockVO> getTopStocks(int limit) {
        try {
            return financeStockMapper.selectLatestStocks(limit);
        } catch (Exception e) {
            log.error("❌ 인기 종목 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> getLatestStocksByDate() {
        try {
            log.info("📊 최신 날짜의 모든 주식 데이터 조회");
            List<FinanceStockVO> stocks = financeStockMapper.selectAllLatestStocks();
            log.info("✅ 최신 날짜 주식 데이터 조회 완료: {}건", stocks.size());
            return stocks;
        } catch (Exception e) {
            log.error("❌ 최신 날짜 주식 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> searchStocksByName(String stockName) {
        if (stockName == null || stockName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 주식명을 입력해주세요");
        }

        try {
            String searchKeyword = stockName.trim();
            log.info("🔍 주식명 검색: '{}'", searchKeyword);

            List<FinanceStockVO> stocks = financeStockMapper.selectByStockName(searchKeyword);
            log.info("✅ '{}' 검색 결과: {}건", searchKeyword, stocks.size());

            return stocks;
        } catch (Exception e) {
            log.error("❌ 주식명 검색 실패 ('{}'): {}", stockName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> searchLatestStocksByName(String stockName, Integer limit) {
        if (stockName == null || stockName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 주식명을 입력해주세요");
        }

        try {
            String searchKeyword = stockName.trim();
            int searchLimit = (limit != null && limit > 0) ? limit : 10; // 기본값 10개

            log.info("🔍 최신 주식명 검색: '{}' (최대 {}개)", searchKeyword, searchLimit);

            List<FinanceStockVO> stocks = financeStockMapper.selectLatestByStockName(searchKeyword, searchLimit);
            log.info("✅ '{}' 최신 검색 결과: {}건", searchKeyword, stocks.size());

            return stocks;
        } catch (Exception e) {
            log.error("❌ 최신 주식명 검색 실패 ('{}'): {}", stockName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> searchStocksByExactName(String stockName) {
        if (stockName == null || stockName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 주식명을 입력해주세요");
        }

        try {
            String searchKeyword = stockName.trim();
            log.info("🔍 정확한 주식명 검색: '{}'", searchKeyword);

            List<FinanceStockVO> stocks = financeStockMapper.selectByExactStockName(searchKeyword);
            log.info("✅ '{}' 정확한 검색 결과: {}건", searchKeyword, stocks.size());

            return stocks;
        } catch (Exception e) {
            log.error("❌ 정확한 주식명 검색 실패 ('{}'): {}", stockName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> searchLatestStocksByExactName(String stockName, Integer limit) {
        if (stockName == null || stockName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 주식명을 입력해주세요");
        }

        try {
            String searchKeyword = stockName.trim();
            int searchLimit = (limit != null && limit > 0) ? limit : 10; // 기본값 10개

            log.info("🔍 최신 정확한 주식명 검색: '{}' (최대 {}개)", searchKeyword, searchLimit);

            List<FinanceStockVO> stocks = financeStockMapper.selectLatestByExactStockName(searchKeyword, searchLimit);
            log.info("✅ '{}' 최신 정확한 검색 결과: {}건", searchKeyword, stocks.size());

            return stocks;
        } catch (Exception e) {
            log.error("❌ 최신 정확한 주식명 검색 실패 ('{}'): {}", stockName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<FinanceStockVO> searchStocksByCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 종목코드를 입력해주세요");
        }

        try {
            String searchCode = stockCode.trim();
            log.info("🔍 종목코드 검색: '{}'", searchCode);

            List<FinanceStockVO> stocks = financeStockMapper.selectByStockCode(searchCode);
            log.info("✅ '{}' 종목코드 검색 결과: {}건", searchCode, stocks.size());

            return stocks;
        } catch (Exception e) {
            log.error("❌ 종목코드 검색 실패 ('{}'): {}", stockCode, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public boolean updateStock(FinanceStockVO financeStock) {
        try {
            int result = financeStockMapper.update(financeStock);
            return result > 0;
        } catch (Exception e) {
            log.error("❌ 주식 데이터 업데이트 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isStockExists(String stockCode, LocalDate baseDate) {
        try {
            int count = financeStockMapper.existsByCodeAndDate(stockCode, baseDate);
            return count > 0;
        } catch (Exception e) {
            log.error("❌ 주식 데이터 존재 여부 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * StockSecurityInfoDto를 FinanceStockVO로 변환
     * 새로운 테이블 구조에 맞게 매핑
     */
    private FinanceStockVO convertDtoToVo(StockSecurityInfoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("변환할 DTO가 null입니다");
        }

        try {
            log.debug("🔄 DTO 변환 시작: 종목코드={}, 종목명={}", dto.getShortCode(), dto.getItemName());

            // 기준일자 파싱 (YYYYMMDD -> LocalDate)
            LocalDate baseDate;
            if (dto.getBaseDate() != null && !dto.getBaseDate().trim().isEmpty()) {
                try {
                    baseDate = LocalDate.parse(dto.getBaseDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    log.debug("📅 기준일자 변환 성공: {} -> {}", dto.getBaseDate(), baseDate);
                } catch (Exception e) {
                    log.warn("⚠️ 기준일자 변환 실패: {}", dto.getBaseDate());
                    throw new RuntimeException("기준일자 변환 실패: " + dto.getBaseDate());
                }
            } else {
                log.warn("⚠️ 기준일자가 null이거나 비어있음");
                throw new RuntimeException("기준일자가 null이거나 비어있음");
            }

            FinanceStockVO vo = new FinanceStockVO();

            // 새로운 테이블 구조에 맞게 매핑
            vo.setBasDt(baseDate);
            vo.setSrtnCd(dto.getShortCode());
            vo.setIsinCd(dto.getIsinCode());
            vo.setItmsNm(dto.getItemName());
            vo.setMrktCtg(dto.getMarketCategory());

            // 가격 정보
            vo.setClpr(parseLongValue(dto.getClosePrice())); // 종가
            vo.setVs(parseLongValue(dto.getVersus())); // 전일 대비 등락
            vo.setFltRt(parseBigDecimalValue(dto.getFluctuationRate())); // 등락률
            vo.setMkp(parseLongValue(dto.getMarketPrice())); // 시가
            vo.setHipr(parseLongValue(dto.getHighPrice())); // 고가
            vo.setLopr(parseLongValue(dto.getLowPrice())); // 저가

            // 거래 정보
            vo.setTrqu(parseLongValue(dto.getTradingQuantity())); // 거래량
            vo.setTrPrc(parseLongValue(dto.getTradingPrice())); // 거래대금

            // 시장 정보
            vo.setLstgStCnt(parseLongValue(dto.getListedStockCount())); // 상장주식수
            vo.setMrktTotAmt(parseLongValue(dto.getMarketTotalAmount())); // 시가총액

            return vo;

        } catch (Exception e) {
            log.error("❌ DTO 변환 실패 (종목: {}): {}", dto.getShortCode(), e.getMessage());
            throw new RuntimeException("DTO 변환 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 문자열을 BigDecimal로 안전하게 변환
     */
    private BigDecimal parseBigDecimalValue(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("⚠️ BigDecimal 변환 실패: {}", value);
            return null;
        }
    }

    /**
     * 문자열을 Long으로 안전하게 변환
     */
    private Long parseLongValue(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        try {
            return Long.parseLong(value.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("⚠️ Long 변환 실패: {}", value);
            return null;
        }
    }
}

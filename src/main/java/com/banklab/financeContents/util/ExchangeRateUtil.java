package com.banklab.financeContents.util;

import com.banklab.financeContents.dto.ExchangeRateDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 환율 관련 유틸리티 클래스
 * 
 * 주요 기능:
 * - 환율 계산 (원화 ↔ 외화)
 * - 송금 비용 계산 (TTB/TTS 적용)
 * - 날짜 포맷 변환 및 검증
 * - 통화별 환율 검색
 * - 환율 변화율 계산
 */
@Slf4j
public class ExchangeRateUtil {
    
    /** 한국 통화 형식 포맷터 */
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.KOREA);
    
    /**
     * 현재 날짜를 YYYYMMDD 형식으로 반환
     * API 호출 시 사용할 오늘 날짜를 생성합니다.
     * 
     * @return 현재 날짜 문자열 (예: "20250722")
     */
    public static String getCurrentDateString() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }
    
    /**
     * 어제 날짜를 YYYYMMDD 형식으로 반환
     * @return 어제 날짜 문자열
     */
    public static String getYesterdayDateString() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        return new SimpleDateFormat("yyyyMMdd").format(yesterday);
    }
    
    /**
     * 통화 코드로 환율 정보 찾기
     * 환율 리스트에서 특정 통화의 정보를 검색합니다.
     * 
     * @param exchangeRates 환율 정보 리스트
     * @param currencyCode 찾을 통화 코드 (예: "USD", "EUR")
     * @return 해당 통화의 환율 정보, 없으면 null
     */
    public static ExchangeRateDto findByCurrencyCode(List<ExchangeRateDto> exchangeRates, String currencyCode) {
        return exchangeRates.stream()
            .filter(rate -> currencyCode.equals(rate.getCur_unit()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 원화를 외화로 환전 계산
     * 매매기준율을 사용하여 원화를 외화로 변환합니다.
     * 
     * @param krwAmount 원화 금액
     * @param exchangeRate 환율 정보
     * @return 환전된 외화 금액 (소수점 둘째자리 반올림)
     */
    public static BigDecimal convertKrwToForeign(BigDecimal krwAmount, ExchangeRateDto exchangeRate) {
        try {
            String dealBasRate = exchangeRate.getDeal_bas_r().replace(",", "");
            BigDecimal rate = new BigDecimal(dealBasRate);
            return krwAmount.divide(rate, 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("원화 -> 외화 환전 계산 오류", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 외화를 원화로 환전 계산
     * @param foreignAmount 외화 금액
     * @param exchangeRate 환율 정보
     * @return 환전된 원화 금액
     */
    public static BigDecimal convertForeignToKrw(BigDecimal foreignAmount, ExchangeRateDto exchangeRate) {
        try {
            String dealBasRate = exchangeRate.getDeal_bas_r().replace(",", "");
            BigDecimal rate = new BigDecimal(dealBasRate);
            return foreignAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("외화 -> 원화 환전 계산 오류", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 송금 시 받을 수 있는 금액 계산 (TTB 적용)
     * 외화를 받을 때 적용되는 전신환매입율을 사용합니다.
     * 
     * @param foreignAmount 외화 금액
     * @param exchangeRate 환율 정보
     * @return 받을 수 있는 원화 금액 (원 단위 반올림)
     */
    public static BigDecimal calculateReceiveAmount(BigDecimal foreignAmount, ExchangeRateDto exchangeRate) {
        try {
            String ttbRate = exchangeRate.getTtb().replace(",", "");
            BigDecimal rate = new BigDecimal(ttbRate);
            return foreignAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("송금 받을 금액 계산 오류", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 송금 시 필요한 금액 계산 (TTS 적용)
     * 외화를 보낼 때 적용되는 전신환매도율을 사용합니다.
     * 
     * @param foreignAmount 외화 금액
     * @param exchangeRate 환율 정보
     * @return 필요한 원화 금액 (원 단위 반올림)
     */
    public static BigDecimal calculateSendAmount(BigDecimal foreignAmount, ExchangeRateDto exchangeRate) {
        try {
            String ttsRate = exchangeRate.getTts().replace(",", "");
            BigDecimal rate = new BigDecimal(ttsRate);
            return foreignAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("송금 필요 금액 계산 오류", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 금액을 한국 통화 형식으로 포맷
     * @param amount 금액
     * @return 포맷된 금액 문자열
     */
    public static String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * 환율 변화율 계산
     * 현재 환율과 이전 환율을 비교하여 변화율을 계산합니다.
     * 
     * @param currentRate 현재 환율
     * @param previousRate 이전 환율
     * @return 변화율 (%, 소수점 넷째자리까지)
     */
    public static BigDecimal calculateChangeRate(BigDecimal currentRate, BigDecimal previousRate) {
        if (previousRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal change = currentRate.subtract(previousRate);
        return change.divide(previousRate, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
    }
    
    /**
     * 날짜 형식 검증
     * @param dateString 날짜 문자열
     * @return 유효한 형식인지 여부
     */
    public static boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.length() != 8) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            sdf.parse(dateString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 주요 통화 코드 목록 반환
     * 자주 사용되는 주요 통화들의 코드를 제공합니다.
     * 
     * @return 주요 통화 코드 배열 (USD, EUR, JPY, CNY, GBP, CHF, CAD, AUD)
     */
    public static String[] getMajorCurrencies() {
        return new String[]{"USD", "EUR", "JPY", "CNY", "GBP", "CHF", "CAD", "AUD"};
    }
}

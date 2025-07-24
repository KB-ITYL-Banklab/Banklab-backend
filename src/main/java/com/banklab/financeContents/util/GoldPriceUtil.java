package com.banklab.financeContents.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 금 시세 관련 유틸리티 클래스
 * 
 * 이 클래스는 금 시세 데이터의 처리, 포맷팅, 검증 등
 * 금 시세 관련 기능에 필요한 다양한 유틸리티 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 금 관련 상품코드 관리 및 검증
 * - 금 시세 데이터 포맷팅 (가격, 중량 단위 등)
 * - 금 순도 및 규격 정보 관리
 * - 통화 단위 변환 및 표시
 * - 금 관련 계산 유틸리티 (온스-그램 변환 등)
 * 
 * 포함된 주요 금 상품 정보:
 * - KRX 금 현물
 * - KRX 금 선물
 * - 국제 금 가격 연동 상품
 * - 기타 금 관련 파생상품
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2025.01
 */
public class GoldPriceUtil {
    
    /** 
     * 주요 금 상품코드와 상품명을 매핑하는 정적 맵
     * KRX에서 거래되는 주요 금 상품들의 정보를 포함합니다.
     */
    private static final Map<String, String> MAJOR_GOLD_PRODUCTS = new HashMap<>();
    
    /** 1온스 = 31.1035 그램 (국제 표준) */
    private static final double OUNCE_TO_GRAM = 31.1035;
    
    /** 1킬로그램 = 1000 그램 */
    private static final double KG_TO_GRAM = 1000.0;
    
    /** 1톤 = 1,000,000 그램 */
    private static final double TON_TO_GRAM = 1000000.0;
    
    /**
     * 정적 초기화 블록
     * 주요 금 상품들을 카테고리별로 분류하여 MAJOR_GOLD_PRODUCTS 맵에 등록합니다.
     */
    static {
        // === KRX 금 현물 상품 ===
        MAJOR_GOLD_PRODUCTS.put("KRX_GOLD_SPOT", "KRX 금 현물");
        MAJOR_GOLD_PRODUCTS.put("GOLD_SPOT_1KG", "금 현물 1kg");
        MAJOR_GOLD_PRODUCTS.put("GOLD_SPOT_100G", "금 현물 100g");
        
        // === KRX 금 선물 상품 ===
        MAJOR_GOLD_PRODUCTS.put("KRX_GOLD_FUT", "KRX 금 선물");
        MAJOR_GOLD_PRODUCTS.put("GOLD_FUT_1KG", "금 선물 1kg");
        MAJOR_GOLD_PRODUCTS.put("MINI_GOLD_FUT", "미니 금 선물");
        
        // === 국제 금 연동 상품 ===
        MAJOR_GOLD_PRODUCTS.put("LONDON_GOLD", "런던 금 연동");
        MAJOR_GOLD_PRODUCTS.put("COMEX_GOLD", "COMEX 금 연동");
        MAJOR_GOLD_PRODUCTS.put("INTERNATIONAL_GOLD", "국제 금 가격");
        
        // 추가 금 상품들은 필요에 따라 계속 등록 가능
    }
    
    /**
     * 금 상품코드의 유효성을 검증합니다.
     * 
     * 일반적으로 금 상품코드는 특정 패턴을 따르거나 등록된 상품 목록에 포함되어야 합니다.
     * 
     * @param productCode 검증할 금 상품코드 문자열
     *                    - null, 빈 문자열, 공백만 있는 경우 false 반환
     *                    - 등록된 주요 상품이거나 일반적인 금 상품 패턴에 맞는 경우 true
     * 
     * @return 유효한 금 상품코드인 경우 true, 그렇지 않으면 false
     * 
     * @see #isMajorGoldProduct(String) 주요 금 상품 여부 확인
     */
    public static boolean isValidGoldProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCode = productCode.trim().toUpperCase();
        
        // 주요 금 상품에 등록된 경우
        if (MAJOR_GOLD_PRODUCTS.containsKey(trimmedCode)) {
            return true;
        }
        
        // 일반적인 금 상품 패턴 검증 (GOLD, AU 등이 포함된 경우)
        return trimmedCode.contains("GOLD") || 
               trimmedCode.contains("AU") || 
               trimmedCode.startsWith("KRX_") ||
               trimmedCode.contains("금");
    }
    
    /**
     * 주어진 상품코드가 주요 금 상품에 포함되는지 확인합니다.
     * 
     * MAJOR_GOLD_PRODUCTS 맵에 등록된 주요 금 상품들에 해당하는지 검사합니다.
     * 
     * @param productCode 확인할 금 상품코드
     *                    - 대소문자 구분 없음 (내부에서 대문자로 변환)
     *                    - null인 경우 false 반환
     * 
     * @return 주요 금 상품에 포함된 경우 true, 그렇지 않으면 false
     * 
     * @see #getGoldProductName(String) 상품코드에 해당하는 상품명 조회
     */
    public static boolean isMajorGoldProduct(String productCode) {
        if (productCode == null) {
            return false;
        }
        return MAJOR_GOLD_PRODUCTS.containsKey(productCode.trim().toUpperCase());
    }
    
    /**
     * 금 상품코드에 해당하는 상품명을 조회합니다.
     * 
     * MAJOR_GOLD_PRODUCTS 맵에서 해당 상품코드의 상품명을 찾아 반환합니다.
     * 등록되지 않은 상품의 경우 null을 반환합니다.
     * 
     * @param productCode 조회할 금 상품코드
     *                    - 대소문자 구분 없음
     * 
     * @return 상품명 문자열
     *         - 등록된 상품: "KRX 금 현물", "금 선물 1kg" 등
     *         - 등록되지 않은 상품: null
     * 
     * @see #isMajorGoldProduct(String) 주요 금 상품 여부 확인
     */
    public static String getGoldProductName(String productCode) {
        if (productCode == null) {
            return null;
        }
        return MAJOR_GOLD_PRODUCTS.get(productCode.trim().toUpperCase());
    }
    
    /**
     * 등록된 모든 주요 금 상품의 코드 목록을 배열로 반환합니다.
     * 
     * MAJOR_GOLD_PRODUCTS 맵의 키(상품코드)들을 문자열 배열로 변환하여 반환합니다.
     * 반환되는 배열의 순서는 보장되지 않습니다.
     * 
     * @return 주요 금 상품코드들의 문자열 배열
     *         - 예: ["KRX_GOLD_SPOT", "GOLD_SPOT_1KG", "KRX_GOLD_FUT", ...]
     *         - 배열 길이: 현재 등록된 주요 금 상품 수
     * 
     * @see #getMajorGoldProducts() 상품코드와 이름이 함께 포함된 맵 반환
     */
    public static String[] getMajorGoldProductCodes() {
        return MAJOR_GOLD_PRODUCTS.keySet().toArray(new String[0]);
    }
    
    /**
     * 주요 금 상품의 코드와 이름 매핑 정보를 맵으로 반환합니다.
     * 
     * MAJOR_GOLD_PRODUCTS의 복사본을 생성하여 반환하므로 외부에서 수정해도
     * 원본 데이터에는 영향을 주지 않습니다.
     * 
     * @return 상품코드를 키로, 상품명을 값으로 하는 HashMap
     *         - 키: "KRX_GOLD_SPOT", 값: "KRX 금 현물"
     *         - 키: "GOLD_SPOT_1KG", 값: "금 현물 1kg"
     *         - 총 개수: 현재 등록된 주요 금 상품 수
     * 
     * @see #getMajorGoldProductCodes() 상품코드만 배열로 반환
     */
    public static Map<String, String> getMajorGoldProducts() {
        return new HashMap<>(MAJOR_GOLD_PRODUCTS);
    }
    
    /**
     * 통화 코드를 사용자 친화적인 한글 통화명으로 변환합니다.
     * 
     * 금 시세 API에서 제공하는 영문 통화 코드를 한글로 변환하여
     * 사용자가 이해하기 쉬운 형태로 제공합니다.
     * 
     * 지원하는 통화:
     * - "KRW" → "원" (한국 원)
     * - "USD" → "달러" (미국 달러)
     * - "EUR" → "유로" (유럽 유로)
     * - "JPY" → "엔" (일본 엔)
     * - "CNY" → "위안" (중국 위안)
     * 
     * @param currencyCode API에서 받은 통화 코드
     *                     - 대소문자 구분 없음 (내부에서 대문자로 변환)
     *                     - null인 경우 "알 수 없음" 반환
     * 
     * @return 한글 통화명
     *         - 알려진 통화: "원", "달러", "유로", "엔", "위안"
     *         - 알려지지 않은 통화: 원본 값 그대로 반환
     *         - null 입력: "알 수 없음"
     */
    public static String getCurrencyName(String currencyCode) {
        if (currencyCode == null) {
            return "알 수 없음";
        }
        
        switch (currencyCode.trim().toUpperCase()) {
            case "KRW":
                return "원";
            case "USD":
                return "달러";
            case "EUR":
                return "유로";
            case "JPY":
                return "엔";
            case "CNY":
                return "위안";
            case "GBP":
                return "파운드";
            default:
                return currencyCode;
        }
    }
    
    /**
     * 금 중량 단위를 변환합니다.
     * 
     * 국제 금 시장에서 사용되는 다양한 중량 단위(온스, 그램, 킬로그램 등)를
     * 그램 기준으로 통일하여 계산 및 비교가 용이하도록 합니다.
     * 
     * 지원하는 변환:
     * - 온스 → 그램: 1 온스 = 31.1035 그램
     * - 킬로그램 → 그램: 1 kg = 1,000 그램
     * - 톤 → 그램: 1 톤 = 1,000,000 그램
     * - 그램 → 그램: 변환 없음 (1:1)
     * 
     * @param weight 변환할 중량 값
     * @param fromUnit 원본 중량 단위
     *                 - "oz", "ounce": 온스
     *                 - "kg", "kilogram": 킬로그램
     *                 - "ton", "tonne": 톤
     *                 - "g", "gram": 그램 (기본값)
     * 
     * @return 그램으로 변환된 중량 값
     *         - 양수 값 반환
     *         - 잘못된 단위인 경우 원본 값 그대로 반환
     * 
     * @throws IllegalArgumentException 중량 값이 음수이거나 0인 경우
     */
    public static double convertWeightToGram(double weight, String fromUnit) {
        if (weight <= 0) {
            throw new IllegalArgumentException("중량 값은 0보다 커야 합니다: " + weight);
        }
        
        if (fromUnit == null) {
            return weight; // 기본값으로 그램으로 간주
        }
        
        switch (fromUnit.trim().toLowerCase()) {
            case "oz":
            case "ounce":
                return weight * OUNCE_TO_GRAM;
            case "kg":
            case "kilogram":
                return weight * KG_TO_GRAM;
            case "ton":
            case "tonne":
                return weight * TON_TO_GRAM;
            case "g":
            case "gram":
            default:
                return weight; // 이미 그램이거나 알 수 없는 단위
        }
    }
    
    /**
     * 그램을 다른 중량 단위로 변환합니다.
     * 
     * @param weightInGram 그램 단위의 중량
     * @param toUnit 변환할 목표 단위
     * @return 변환된 중량 값
     */
    public static double convertGramToWeight(double weightInGram, String toUnit) {
        if (weightInGram <= 0) {
            throw new IllegalArgumentException("중량 값은 0보다 커야 합니다: " + weightInGram);
        }
        
        if (toUnit == null) {
            return weightInGram; // 기본값으로 그램 유지
        }
        
        switch (toUnit.trim().toLowerCase()) {
            case "oz":
            case "ounce":
                return weightInGram / OUNCE_TO_GRAM;
            case "kg":
            case "kilogram":
                return weightInGram / KG_TO_GRAM;
            case "ton":
            case "tonne":
                return weightInGram / TON_TO_GRAM;
            case "g":
            case "gram":
            default:
                return weightInGram; // 이미 그램이거나 알 수 없는 단위
        }
    }
    
    /**
     * 금 가격을 특정 중량 단위당 가격으로 변환합니다.
     * 
     * 예: 1kg당 가격을 1온스당 가격으로 변환
     * 
     * @param price 원본 가격
     * @param fromUnit 원본 중량 단위
     * @param toUnit 목표 중량 단위
     * @return 변환된 단위당 가격
     */
    public static double convertPricePerUnit(double price, String fromUnit, String toUnit) {
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다: " + price);
        }
        
        // 1단위를 그램으로 변환
        double fromUnitInGram = convertWeightToGram(1.0, fromUnit);
        double toUnitInGram = convertWeightToGram(1.0, toUnit);
        
        // 가격 변환: (원본가격 / 원본단위그램수) * 목표단위그램수
        return (price / fromUnitInGram) * toUnitInGram;
    }
    
    /**
     * 금 순도에 따른 실제 금 함량을 계산합니다.
     * 
     * @param totalWeight 전체 중량 (그램)
     * @param purity 순도 (0.0 ~ 1.0 또는 0 ~ 100)
     * @return 실제 금 함량 (그램)
     */
    public static double calculatePureGoldContent(double totalWeight, double purity) {
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("전체 중량은 0보다 커야 합니다: " + totalWeight);
        }
        
        if (purity < 0) {
            throw new IllegalArgumentException("순도는 0 이상이어야 합니다: " + purity);
        }
        
        // 순도가 1보다 크면 퍼센트 단위로 간주하여 100으로 나눔
        double normalizedPurity = purity > 1.0 ? purity / 100.0 : purity;
        
        if (normalizedPurity > 1.0) {
            throw new IllegalArgumentException("순도는 100% 이하여야 합니다: " + purity);
        }
        
        return totalWeight * normalizedPurity;
    }
    
    /**
     * 금 시세 변동률을 시각적으로 구분하기 위한 상태 문자열을 반환합니다.
     * 
     * @param fluctuationRate 등락률 문자열 (예: "1.23", "-2.45")
     * @return 상태 문자열 ("상승", "하락", "보합")
     */
    public static String getPriceChangeStatus(String fluctuationRate) {
        if (fluctuationRate == null || fluctuationRate.trim().isEmpty()) {
            return "보합";
        }
        
        try {
            double rate = Double.parseDouble(fluctuationRate.trim());
            if (rate > 0) {
                return "상승";
            } else if (rate < 0) {
                return "하락";
            } else {
                return "보합";
            }
        } catch (NumberFormatException e) {
            return "알 수 없음";
        }
    }
    
    /**
     * 금 시세 등락률을 이모지와 함께 포맷팅합니다.
     * 
     * @param fluctuationRate 등락률 문자열
     * @return 이모지가 포함된 포맷된 등락률 (예: "🔺 +1.23%", "🔻 -2.45%")
     */
    public static String formatFluctuationRateWithEmoji(String fluctuationRate) {
        if (fluctuationRate == null || fluctuationRate.trim().isEmpty()) {
            return "➖ 0.00%";
        }
        
        try {
            double rate = Double.parseDouble(fluctuationRate.trim());
            String emoji = rate > 0 ? "🔺" : rate < 0 ? "🔻" : "➖";
            String sign = rate > 0 ? "+" : "";
            return String.format("%s %s%.2f%%", emoji, sign, rate);
        } catch (NumberFormatException e) {
            return "❓ " + fluctuationRate + "%";
        }
    }
}
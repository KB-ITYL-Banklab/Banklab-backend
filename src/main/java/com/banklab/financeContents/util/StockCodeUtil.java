package com.banklab.financeContents.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 주식 종목 코드 관련 유틸리티 클래스
 * 
 * 이 클래스는 주식 종목 코드의 검증, 정규화, 주요 종목 정보 관리 등
 * 주식 데이터 처리에 필요한 다양한 유틸리티 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 종목 코드 유효성 검증 (6자리 숫자 형식)
 * - 종목 코드 정규화 (앞자리 0 패딩)
 * - 주요 종목 정보 관리 (대형주, 금융주, 통신주 등)
 * - 시장 구분 코드 변환
 * - 숫자 데이터 포맷팅 (천 단위 콤마, 등락률 등)
 * 
 * 포함된 주요 종목:
 * - 대형주: 삼성전자, SK하이닉스, 네이버, 카카오 등
 * - 금융주: 신한지주, 하나금융지주, 우리금융지주 등
 * - 통신주: KT, SK텔레콤, LG유플러스
 * - 자동차: 현대차, 현대모비스, 기아
 * - 기타: 유통, 소비재 관련 종목들
 */
public class StockCodeUtil {
    
    /** 
     * 주요 종목 코드와 종목명을 매핑하는 정적 맵
     * 시가총액 상위 종목들과 대표적인 업종별 종목들을 포함합니다.
     */
    private static final Map<String, String> MAJOR_STOCKS = new HashMap<>();
    
    /**
     * 정적 초기화 블록
     * 주요 종목들을 업종별로 분류하여 MAJOR_STOCKS 맵에 등록합니다.
     */
    static {
        // === 대형주 (시가총액 기준 상위 종목) ===
        MAJOR_STOCKS.put("005930", "삼성전자");
        MAJOR_STOCKS.put("000660", "SK하이닉스");
        MAJOR_STOCKS.put("035420", "네이버");
        MAJOR_STOCKS.put("035720", "카카오");
        MAJOR_STOCKS.put("051910", "LG화학");
        MAJOR_STOCKS.put("006400", "삼성SDI");
        MAJOR_STOCKS.put("373220", "LG에너지솔루션");
        MAJOR_STOCKS.put("207940", "삼성바이오로직스");
        MAJOR_STOCKS.put("068270", "셀트리온");
        MAJOR_STOCKS.put("003670", "포스코홀딩스");
        
        // === 금융주 (은행, 보험, 증권 등) ===
        MAJOR_STOCKS.put("055550", "신한지주");
        MAJOR_STOCKS.put("086790", "하나금융지주");
        MAJOR_STOCKS.put("316140", "우리금융지주");
        MAJOR_STOCKS.put("105560", "KB금융");
        MAJOR_STOCKS.put("032830", "삼성생명");
        
        // === 통신주 (이동통신, 인터넷 서비스 등) ===
        MAJOR_STOCKS.put("030200", "KT");
        MAJOR_STOCKS.put("017670", "SK텔레콤");
        MAJOR_STOCKS.put("032640", "LG유플러스");
        
        // === 자동차 (완성차, 부품 등) ===
        MAJOR_STOCKS.put("005380", "현대차");
        MAJOR_STOCKS.put("012330", "현대모비스");
        MAJOR_STOCKS.put("000270", "기아");
        
        // === 유통/소비재 (식품, 유통, 생활용품 등) ===
        MAJOR_STOCKS.put("097950", "CJ제일제당");
        MAJOR_STOCKS.put("271560", "오리온");
        MAJOR_STOCKS.put("282330", "BGF리테일");
        MAJOR_STOCKS.put("051600", "한전KPS");
        
        // 추가 종목들은 필요에 따라 계속 등록 가능
    }
    
    /**
     * 종목 코드의 유효성을 검증합니다.
     * 
     * 한국 주식시장의 종목 코드는 6자리 숫자로 구성됩니다.
     * 예: "005930" (삼성전자), "000660" (SK하이닉스)
     * 
     * @param stockCode 검증할 종목 코드 문자열
     *                  - null, 빈 문자열, 공백만 있는 경우 false 반환
     *                  - 6자리 숫자가 아닌 경우 false 반환
     * 
     * @return 유효한 종목 코드인 경우 true, 그렇지 않으면 false
     * 
     * @see #normalizeStockCode(String) 종목 코드 정규화
     */
    public static boolean isValidStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCode = stockCode.trim();
        
        // 6자리 숫자 확인
        return trimmedCode.matches("^\\d{6}$");
    }
    
    /**
     * 종목 코드를 표준 6자리 형식으로 정규화합니다.
     * 
     * 입력된 숫자 문자열을 6자리로 패딩하여 표준 종목 코드 형식으로 변환합니다.
     * 예: "5930" → "005930", "660" → "000660"
     * 
     * 동작 방식:
     * 1. null 입력시 null 반환
     * 2. 숫자가 아닌 문자가 포함된 경우 null 반환  
     * 3. 유효한 숫자인 경우 앞자리를 0으로 패딩하여 6자리로 변환
     * 
     * @param stockCode 정규화할 종목 코드 (숫자 문자열)
     *                  - 예: "5930", "660", "123456"
     * 
     * @return 정규화된 6자리 종목 코드 문자열
     *         - 성공시: "005930", "000660" 등
     *         - 실패시: null (잘못된 입력)
     * 
     * @throws NumberFormatException 숫자 변환 실패시 (내부적으로 처리하여 null 반환)
     */
    public static String normalizeStockCode(String stockCode) {
        if (stockCode == null) {
            return null;
        }
        
        String trimmedCode = stockCode.trim();
        
        // 숫자가 아닌 경우 null 반환
        if (!trimmedCode.matches("^\\d+$")) {
            return null;
        }
        
        // 6자리로 패딩
        return String.format("%06d", Integer.parseInt(trimmedCode));
    }
    
    /**
     * 주어진 종목 코드가 주요 종목에 포함되는지 확인합니다.
     * 
     * MAJOR_STOCKS 맵에 등록된 종목들(대형주, 금융주, 통신주 등)에
     * 해당하는지 검사합니다.
     * 
     * @param stockCode 확인할 종목 코드 (6자리 문자열)
     *                  - 예: "005930" (삼성전자), "000660" (SK하이닉스)
     * 
     * @return 주요 종목에 포함된 경우 true, 그렇지 않으면 false
     *         - null 입력시 false 반환
     * 
     * @see #getMajorStocks() 전체 주요 종목 목록 조회
     */
    public static boolean isMajorStock(String stockCode) {
        return MAJOR_STOCKS.containsKey(stockCode);
    }
    
    /**
     * 종목 코드에 해당하는 종목명을 조회합니다.
     * 
     * MAJOR_STOCKS 맵에서 해당 종목 코드의 종목명을 찾아 반환합니다.
     * 등록되지 않은 종목의 경우 null을 반환합니다.
     * 
     * @param stockCode 조회할 종목 코드 (6자리 문자열)
     *                  - 예: "005930", "000660"
     * 
     * @return 종목명 문자열
     *         - 등록된 종목: "삼성전자", "SK하이닉스" 등
     *         - 등록되지 않은 종목: null
     * 
     * @see #isMajorStock(String) 주요 종목 여부 확인
     */
    public static String getStockName(String stockCode) {
        return MAJOR_STOCKS.get(stockCode);
    }
    
    /**
     * 등록된 모든 주요 종목의 코드 목록을 배열로 반환합니다.
     * 
     * MAJOR_STOCKS 맵의 키(종목 코드)들을 문자열 배열로 변환하여 반환합니다.
     * 반환되는 배열의 순서는 보장되지 않습니다.
     * 
     * @return 주요 종목 코드들의 문자열 배열
     *         - 예: ["005930", "000660", "035420", ...]
     *         - 배열 길이: 현재 등록된 주요 종목 수
     * 
     * @see #getMajorStocks() 종목 코드와 이름이 함께 포함된 맵 반환
     */
    public static String[] getMajorStockCodes() {
        return MAJOR_STOCKS.keySet().toArray(new String[0]);
    }
    
    /**
     * 주요 종목의 코드와 이름 매핑 정보를 맵으로 반환합니다.
     * 
     * MAJOR_STOCKS의 복사본을 생성하여 반환하므로 외부에서 수정해도
     * 원본 데이터에는 영향을 주지 않습니다.
     * 
     * @return 종목 코드를 키로, 종목명을 값으로 하는 HashMap
     *         - 키: "005930", 값: "삼성전자"
     *         - 키: "000660", 값: "SK하이닉스"
     *         - 총 개수: 현재 등록된 주요 종목 수
     * 
     * @see #getMajorStockCodes() 종목 코드만 배열로 반환
     */
    public static Map<String, String> getMajorStocks() {
        return new HashMap<>(MAJOR_STOCKS);
    }
    
    /**
     * API에서 받은 시장 구분 코드를 사용자 친화적인 한글 시장명으로 변환합니다.
     * 
     * 공공데이터포털 API에서 제공하는 영문 시장 구분을 한글로 변환하여
     * 사용자가 이해하기 쉬운 형태로 제공합니다.
     * 
     * 지원하는 시장 구분:
     * - "KOSPI" → "코스피" (Korea Composite Stock Price Index)
     * - "KOSDAQ" → "코스닥" (Korea Securities Dealers Automated Quotations)
     * - "KONEX" → "코넥스" (Korea New Exchange)
     * 
     * @param marketCategory API에서 받은 시장 구분 코드
     *                       - 대소문자 구분 없음 (내부에서 대문자로 변환)
     *                       - null인 경우 "알 수 없음" 반환
     * 
     * @return 한글 시장명
     *         - 알려진 시장: "코스피", "코스닥", "코넥스"
     *         - 알려지지 않은 시장: 원본 값 그대로 반환
     *         - null 입력: "알 수 없음"
     */
    public static String getMarketName(String marketCategory) {
        if (marketCategory == null) {
            return "알 수 없음";
        }
        
        switch (marketCategory.toUpperCase()) {
            case "KOSPI":
                return "코스피";
            case "KOSDAQ":
                return "코스닥";
            case "KONEX":
                return "코넥스";
            default:
                return marketCategory;
        }
    }
    
    /**
     * 숫자 문자열을 천 단위 콤마가 포함된 형태로 포맷팅합니다.
     * 
     * 주식 데이터의 가격, 거래량, 시가총액 등 큰 숫자를 사용자가
     * 읽기 쉽도록 천 단위마다 콤마를 추가합니다.
     * 
     * 예시:
     * - "1000" → "1,000"
     * - "1000000" → "1,000,000"
     * - "123456789" → "123,456,789"
     * 
     * @param value 포맷팅할 숫자 문자열
     *              - null이나 빈 문자열인 경우 "0" 반환
     *              - 숫자가 아닌 경우 원본 값 그대로 반환
     * 
     * @return 천 단위 콤마가 추가된 숫자 문자열
     *         - 성공시: "1,000,000" 형태
     *         - 실패시: 원본 값 또는 "0"
     * 
     * @throws NumberFormatException 숫자 파싱 실패시 (내부적으로 처리하여 원본값 반환)
     */
    public static String formatNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "0";
        }
        
        try {
            long number = Long.parseLong(value.trim());
            return String.format("%,d", number);
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    /**
     * 등락률을 사용자 친화적인 형태로 포맷팅합니다.
     * 
     * 주식의 등락률에 적절한 부호(+/-)와 퍼센트 기호를 추가하여
     * 시각적으로 구분하기 쉽도록 포맷팅합니다.
     * 
     * 포맷팅 규칙:
     * - 양수: "+" 기호 추가 (예: "+1.23%")
     * - 음수: "-" 기호 유지 (예: "-2.45%")
     * - 0: "0.00%" (기호 없음)
     * - 소수점 둘째 자리까지 표시
     * 
     * 예시:
     * - "1.23" → "+1.23%"
     * - "-2.45" → "-2.45%"
     * - "0" → "0.00%"
     * 
     * @param fluctuationRate 등락률 숫자 문자열
     *                        - null이나 빈 문자열인 경우 "0.00%" 반환
     *                        - 숫자가 아닌 경우 원본값 + "%" 반환
     * 
     * @return 포맷된 등락률 문자열
     *         - 성공시: "+1.23%", "-2.45%" 형태
     *         - 실패시: 원본값 + "%" 또는 "0.00%"
     * 
     * @throws NumberFormatException 숫자 파싱 실패시 (내부적으로 처리)
     */
    public static String formatFluctuationRate(String fluctuationRate) {
        if (fluctuationRate == null || fluctuationRate.trim().isEmpty()) {
            return "0.00%";
        }
        
        try {
            double rate = Double.parseDouble(fluctuationRate.trim());
            String sign = rate > 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, rate);
        } catch (NumberFormatException e) {
            return fluctuationRate + "%";
        }
    }
}

package com.banklab.financeContents.example;

import com.banklab.financeContents.dto.BitcoinTickerDTO;
import com.banklab.financeContents.service.UpbitApiService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.banklab.config.RootConfig;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 업비트 API 사용 예제 클래스
 * 실제 API 호출을 통해 비트코인 시세를 조회하고 출력합니다.
 */
public class UpbitApiExample {
    
    public static void main(String[] args) {
        try {
            // Spring Context 초기화
            ApplicationContext context = new AnnotationConfigApplicationContext(RootConfig.class);
            UpbitApiService upbitService = context.getBean(UpbitApiService.class);
            
            System.out.println("=================================");
            System.out.println("    업비트 API 연동 테스트");
            System.out.println("=================================");
            
            // 1. API 연결 상태 확인
            System.out.println("\n1. API 연결 상태 확인...");
            boolean isAvailable = upbitService.isApiAvailable();
            System.out.println("API 상태: " + (isAvailable ? "정상" : "연결 불가"));
            
            if (!isAvailable) {
                System.out.println("API 연결에 문제가 있습니다. 네트워크 상태를 확인해주세요.");
                return;
            }
            
            // 2. 비트코인 현재가 조회
            System.out.println("\n2. 비트코인 현재가 조회...");
            Double bitcoinPrice = upbitService.getBitcoinPrice();
            if (bitcoinPrice != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
                System.out.println("비트코인 현재가: " + formatter.format(bitcoinPrice) + " KRW");
            } else {
                System.out.println("비트코인 가격 조회 실패");
            }
            
            // 3. 비트코인 상세 정보 조회
            System.out.println("\n3. 비트코인 상세 정보 조회...");
            BitcoinTickerDTO ticker = upbitService.getBitcoinTicker();
            if (ticker != null) {
                printBitcoinDetail(ticker);
            } else {
                System.out.println("비트코인 상세 정보 조회 실패");
            }
            
            // 4. 여러 암호화폐 시세 조회
            System.out.println("\n4. 주요 암호화폐 시세 조회...");
            List<BitcoinTickerDTO> tickers = upbitService.getMultipleTickers("KRW-BTC,KRW-ETH,KRW-XRP");
            if (tickers != null && !tickers.isEmpty()) {
                printMultipleTickers(tickers);
            } else {
                System.out.println("다중 암호화폐 시세 조회 실패");
            }
            
            System.out.println("\n=================================");
            System.out.println("    테스트 완료");
            System.out.println("=================================");
            
        } catch (Exception e) {
            System.err.println("예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 비트코인 상세 정보를 출력하는 메서드
     */
    private static void printBitcoinDetail(BitcoinTickerDTO ticker) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│             비트코인 상세 정보                 │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│ 마켓: " + ticker.getMarket());
        System.out.println("│ 현재가: " + formatter.format(ticker.getTradePrice()) + " KRW");
        System.out.println("│ 전일 종가: " + formatter.format(ticker.getPrevClosingPrice()) + " KRW");
        System.out.println("│ 시가: " + formatter.format(ticker.getOpeningPrice()) + " KRW");
        System.out.println("│ 고가: " + formatter.format(ticker.getHighPrice()) + " KRW");
        System.out.println("│ 저가: " + formatter.format(ticker.getLowPrice()) + " KRW");
        
        // 변동률 계산 및 표시
        String changeDirection = getChangeDirection(ticker.getChange());
        String changeRate = String.format("%.2f", Math.abs(ticker.getChangeRate() * 100));
        String changePrice = formatter.format(Math.abs(ticker.getChangePrice()));
        
        System.out.println("│ 전일 대비: " + changeDirection + " " + changePrice + " KRW (" + changeRate + "%)");
        System.out.println("│ 거래량: " + formatter.format(ticker.getTradeVolume()) + " BTC");
        System.out.println("│ 거래대금: " + formatter.format(ticker.getAccTradePrice24h()) + " KRW");
        System.out.println("│ 52주 최고가: " + formatter.format(ticker.getHighest52WeekPrice()) + " KRW");
        System.out.println("│ 52주 최저가: " + formatter.format(ticker.getLowest52WeekPrice()) + " KRW");
        System.out.println("└─────────────────────────────────────────────┘");
    }
    
    /**
     * 여러 암호화폐 시세를 출력하는 메서드
     */
    private static void printMultipleTickers(List<BitcoinTickerDTO> tickers) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│                        주요 암호화폐 시세                          │");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        System.out.println("│ 종목      │     현재가     │    변동률    │       거래량       │");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        
        for (BitcoinTickerDTO ticker : tickers) {
            String market = ticker.getMarket().replace("KRW-", "");
            String price = formatter.format(ticker.getTradePrice());
            String changeDirection = getChangeDirection(ticker.getChange());
            String changeRate = String.format("%.2f", Math.abs(ticker.getChangeRate() * 100));
            String volume = formatter.format(ticker.getTradeVolume());
            
            System.out.printf("│ %-8s │ %13s │ %s %6s%% │ %16s │%n", 
                market, price + " KRW", changeDirection, changeRate, volume);
        }
        
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
    }
    
    /**
     * 변동 방향을 한글로 변환하는 메서드
     */
    private static String getChangeDirection(String change) {
        switch (change) {
            case "RISE":
                return "↗ 상승";
            case "FALL":
                return "↘ 하락";
            case "EVEN":
                return "→ 보합";
            default:
                return "? 알수없음";
        }
    }
}

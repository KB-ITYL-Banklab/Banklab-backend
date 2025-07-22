package com.banklab.financeContents.service;

import com.banklab.config.RootConfig;
import com.banklab.financeContents.dto.BitcoinTickerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class UpbitApiServiceTest
 * @description UpbitApiService의 비즈니스 로직이 올바르게 동작하는지 검증하는 단위 테스트 클래스입니다.
 * - JUnit 5와 Spring TestContext Framework를 사용하여 테스트 환경을 구성합니다.
 * - 각 테스트 메서드는 서비스의 특정 기능을 호출하고, 그 결과가 예상과 일치하는지 확인합니다.
 */
@SpringJUnitConfig(classes = {RootConfig.class}) // Spring 설정 파일(RootConfig.class)을 로드하여 테스트 환경을 설정합니다.
public class UpbitApiServiceTest {

    // 테스트 대상인 UpbitApiService를 스프링 컨테이너로부터 주입받습니다.
    @Autowired
    private UpbitApiService upbitApiService;

    /**
     * @method testGetBitcoinTicker
     * @description getBitcoinTicker() 메서드가 업비트 API로부터 비트코인 시세 정보를 정확히 받아오는지 테스트합니다.
     * - 결과 객체가 null이 아닌지, 마켓 코드가 'KRW-BTC'인지, 거래 가격이 유효한지 등을 검증합니다.
     */
    @Test
    public void testGetBitcoinTicker() {
        System.out.println("비트코인 시세 조회 테스트 시작");

        // When: 실제 테스트할 동작을 수행합니다.
        // 서비스의 getBitcoinTicker 메서드를 호출하여 결과를 받습니다.
        BitcoinTickerDTO ticker = upbitApiService.getBitcoinTicker();

        // Then: 수행된 동작의 결과를 검증합니다.
        // ticker 객체가 null이 아니어야 테스트 통과
        assertNotNull(ticker, "비트코인 시세 정보가 null이 아니어야 합니다.");
        // 마켓 코드가 "KRW-BTC"인지 확인
        assertEquals("KRW-BTC", ticker.getMarket(), "마켓 코드가 KRW-BTC여야 합니다.");
        // 현재 체결가가 null이 아닌지 확인
        assertNotNull(ticker.getTradePrice(), "거래가격이 null이 아니어야 합니다.");
        // 현재 체결가가 0보다 큰 양수인지 확인
        assertTrue(ticker.getTradePrice() > 0, "거래가격이 0보다 커야 합니다.");

        // 테스트 결과 콘솔 출력
        System.out.println("비트코인 현재가: " + ticker.getTradePrice() + " KRW");
        System.out.println("전일 대비: " + ticker.getChange()); // 'RISE' (상승), 'FALL' (하락), 'EVEN' (보합)
        System.out.println("변동률: " + (ticker.getChangeRate() * 100) + "%");
    }

    /**
     * @method testGetBitcoinPrice
     * @description getBitcoinPrice() 메서드가 비트코인의 현재 가격을 Double 타입으로 정확히 반환하는지 테스트합니다.
     * - 결과 가격이 null이 아니고 0보다 큰 값인지 검증합니다.
     */
    @Test
    public void testGetBitcoinPrice() {
        System.out.println("비트코인 현재가 조회 테스트 시작");

        // When: 서비스의 getBitcoinPrice 메서드를 호출합니다.
        Double price = upbitApiService.getBitcoinPrice();

        // Then: 결과를 검증합니다.
        // 가격이 null이 아니어야 함
        assertNotNull(price, "비트코인 가격이 null이 아니어야 합니다.");
        // 가격이 0보다 커야 함
        assertTrue(price > 0, "비트코인 가격이 0보다 커야 합니다.");

        // 테스트 결과 콘솔 출력
        System.out.println("비트코인 현재가: " + price + " KRW");
    }

    /**
     * @method testGetMultipleTickers
     * @description getMultipleTickers() 메서드가 여러 암호화폐의 시세 정보를 리스트 형태로 성공적으로 가져오는지 테스트합니다.
     * - 요청한 마켓 수만큼 응답이 왔는지, 각 항목의 데이터가 유효한지 검증합니다.
     */
    @Test
    public void testGetMultipleTickers() {
        System.out.println("다중 암호화폐 시세 조회 테스트 시작");

        // Given: 테스트에 필요한 사전 데이터를 설정합니다.
        // 조회할 마켓 코드들을 쉼표로 구분하여 정의합니다.
        String markets = "KRW-BTC,KRW-ETH,KRW-XRP";

        // When: 실제 테스트 동작을 수행합니다.
        List<BitcoinTickerDTO> tickers = upbitApiService.getMultipleTickers(markets);

        // Then: 결과를 검증합니다.
        // 반환된 리스트가 null이 아니어야 함
        assertNotNull(tickers, "시세 정보 리스트가 null이 아니어야 합니다.");
        // 요청한 마켓의 개수(3개)와 동일한 크기의 리스트가 반환되었는지 확인
        assertEquals(3, tickers.size(), "3개의 암호화폐 정보가 있어야 합니다.");

        // 반복문을 통해 리스트의 모든 항목을 검증합니다.
        for (BitcoinTickerDTO ticker : tickers) {
            assertNotNull(ticker.getMarket(), "마켓 코드가 null이 아니어야 합니다.");
            assertNotNull(ticker.getTradePrice(), "거래가격이 null이 아니어야 합니다.");
            assertTrue(ticker.getTradePrice() > 0, "거래가격이 0보다 커야 합니다.");

            // 각 암호화폐의 정보 콘솔 출력
            System.out.println(ticker.getMarket() + ": " + ticker.getTradePrice() + " KRW");
        }
    }

    /**
     * @method testIsApiAvailable
     * @description isApiAvailable() 메서드가 업비트 API의 현재 상태를 올바르게 확인하는지 테스트합니다.
     * - API가 정상적으로 호출 가능한 상태(true)인지 검증합니다.
     */
    @Test
    public void testIsApiAvailable() {
        System.out.println("API 연결 상태 확인 테스트 시작");

        // When: 서비스의 isApiAvailable 메서드를 호출합니다.
        boolean isAvailable = upbitApiService.isApiAvailable();

        // Then: 결과를 검증합니다.
        // API 상태가 true (사용 가능)인지 확인
        assertTrue(isAvailable, "API가 사용 가능해야 합니다.");

        // 테스트 결과 콘솔 출력
        System.out.println("API 연결 상태: " + (isAvailable ? "정상" : "불가능"));
    }
}
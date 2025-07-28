package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.ExchangeRateDto;
import com.banklab.financeContents.dto.ExchangeRateResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 한국수출입은행 환율 API 서비스
 * 
 * 주요 기능:
 * - 실시간 환율 정보 조회
 * - 특정 날짜별 환율 조회
 * - 통화별 환율 조회
 */
@Slf4j
@Service
public class ExchangeRateService {
    
    @Value("${exchange.api.url}")
    private String API_URL;
    @Value("${exchange.api.key}")
    private String AUTH_KEY;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * 생성자 - HttpClient와 ObjectMapper 초기화
     */
    public ExchangeRateService() {
        this.httpClient = HttpClientBuilder.create().build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 오늘 환율 정보 조회
     * 현재 날짜의 모든 통화 환율 정보를 가져옵니다.
     */
    public ExchangeRateResponse getTodayExchangeRates() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return getExchangeRates(today);
    }
    
    /**
     * 특정 날짜 환율 정보 조회
     * 지정된 날짜의 모든 통화 환율 정보를 가져옵니다.
     */
    public ExchangeRateResponse getExchangeRates(String searchDate) {
        try {
            String url = buildApiUrl(searchDate);
            log.info("환율 API 호출: {}", url);
            
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.debug("API 응답: {}", responseBody);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                List<ExchangeRateDto> exchangeRates = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<ExchangeRateDto>>() {}
                );
                
                return ExchangeRateResponse.builder()
                    .success(true)
                    .message("환율 정보 조회 성공")
                    .data(exchangeRates)
                    .searchDate(searchDate)
                    .count(exchangeRates.size())
                    .build();
                    
            } else {
                log.error("API 호출 실패. 상태코드: {}", response.getStatusLine().getStatusCode());
                return ExchangeRateResponse.builder()
                    .success(false)
                    .message("환율 정보 조회 실패")
                    .searchDate(searchDate)
                    .count(0)
                    .build();
            }
            
        } catch (IOException e) {
            log.error("환율 API 호출 중 오류 발생", e);
            return ExchangeRateResponse.builder()
                .success(false)
                .message("API 호출 중 오류 발생: " + e.getMessage())
                .searchDate(searchDate)
                .count(0)
                .build();
        } catch (Exception e) {
            log.error("환율 데이터 처리 중 오류 발생", e);
            return ExchangeRateResponse.builder()
                .success(false)
                .message("데이터 처리 중 오류 발생: " + e.getMessage())
                .searchDate(searchDate)
                .count(0)
                .build();
        }
    }
    
    /**
     * 특정 통화의 환율 정보 조회
     * 지정된 날짜의 특정 통화 환율만 조회합니다.
     */
    public ExchangeRateResponse getExchangeRateByCurrency(String searchDate, String data) {
        try {
            String url = buildApiUrlWithCurrency(searchDate, data);
            log.info("특정 통화 환율 API 호출: {}", url);
            
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.debug("API 응답: {}", responseBody);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                List<ExchangeRateDto> exchangeRates = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<ExchangeRateDto>>() {}
                );
                
                return ExchangeRateResponse.builder()
                    .success(true)
                    .message("환율 정보 조회 성공")
                    .data(exchangeRates)
                    .searchDate(searchDate)
                    .count(exchangeRates.size())
                    .build();
                    
            } else {
                log.error("API 호출 실패. 상태코드: {}", response.getStatusLine().getStatusCode());
                return ExchangeRateResponse.builder()
                    .success(false)
                    .message("환율 정보 조회 실패")
                    .searchDate(searchDate)
                    .count(0)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("환율 API 호출 중 오류 발생", e);
            return ExchangeRateResponse.builder()
                .success(false)
                .message("API 호출 중 오류 발생: " + e.getMessage())
                .searchDate(searchDate)
                .count(0)
                .build();
        }
    }
    
    /**
     * API URL 생성
     * @param searchDate 조회 날짜
     * @return API URL
     */
    private String buildApiUrl(String searchDate) {
        return String.format("%s?authkey=%s&searchdate=%s&data=AP01", 
            API_URL, AUTH_KEY, searchDate);
    }
    
    /**
     * 특정 통화 API URL 생성
     * @param searchDate 조회 날짜
     * @param data 통화 코드
     * @return API URL
     */
    private String buildApiUrlWithCurrency(String searchDate, String data) {
        return String.format("%s?authkey=%s&searchdate=%s&data=%s", 
            API_URL, AUTH_KEY, searchDate, data);
    }
}

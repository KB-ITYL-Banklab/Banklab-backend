package com.banklab.product.service.mortgage;

import com.banklab.product.dto.mortgage.MortgageLoanApiResultWrapper;
import com.banklab.product.dto.mortgage.MortgageLoanProductAndOptionListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 주택담보대출 상품 fetch API
 */
@Service
public class MortgageLoanApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_URL = "https://finlife.fss.or.kr/finlifeapi/mortgageLoanProductsSearch.json";
    @Value("${finlife.api-key}")
    private String API_KEY;

    public MortgageLoanProductAndOptionListDto fetchProductsFromApi() {
        String fullUrl = API_URL + "?auth=" + API_KEY + "&topFinGrpNo=020000&pageNo=1";

        System.out.println("Mortgage Loan API 호출 URL: " + fullUrl);

        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            // String으로 먼저 응답 받기
            ResponseEntity<String> stringResponse = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Mortgage Loan API 응답 상태: " + stringResponse.getStatusCode());
            System.out.println("Mortgage Loan API 응답 Body: " + stringResponse.getBody());

            if (stringResponse.getBody() == null || stringResponse.getBody().isEmpty()) {
                throw new RuntimeException("Mortgage Loan API 응답이 비어있습니다.");
            }

            // MortgageLoanApiResultWrapper로 파싱
            ResponseEntity<MortgageLoanApiResultWrapper> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    MortgageLoanApiResultWrapper.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Mortgage Loan API 응답 파싱 실패. JSON 구조를 확인해주세요.");
            }

            MortgageLoanApiResultWrapper.MortgageLoanApiResult result = response.getBody().getResult();

            if (result == null) {
                throw new RuntimeException("Mortgage Loan API 결과 데이터가 null입니다.");
            }

            MortgageLoanProductAndOptionListDto dto = new MortgageLoanProductAndOptionListDto();

            dto.setProducts(result.getBaseList().stream()
                    .collect(Collectors.toList()));

            dto.setOptions(result.getOptionList().stream()
                    .collect(Collectors.toList()));

            System.out.println("Mortgage Loan API 호출 성공: 상품 " + result.getBaseList().size() + "개, 옵션 " + result.getOptionList().size() + "개");

            return dto;

        } catch (ResourceAccessException e) {
            System.err.println("Mortgage Loan API 네트워크 연결 오류: " + e.getMessage());
            throw new RuntimeException("Mortgage Loan API 서버에 연결할 수 없습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Mortgage Loan API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Mortgage Loan API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}

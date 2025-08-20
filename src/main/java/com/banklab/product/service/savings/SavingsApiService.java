package com.banklab.product.service.savings;

import com.banklab.product.dto.savings.SavingsApiResultWrapper;
import com.banklab.product.dto.savings.SavingsProductAndOptionListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 적금 상품 fetch API
 */
@Service
public class SavingsApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_URL = "https://finlife.fss.or.kr/finlifeapi/savingProductsSearch.json";
    @Value("${finlife.api-key}")
    private String API_KEY;

    public SavingsProductAndOptionListDto fetchProductsFromApi() {
        String fullUrl = API_URL + "?auth=" + API_KEY + "&topFinGrpNo=020000&pageNo=1";

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

            if (stringResponse.getBody() == null || stringResponse.getBody().isEmpty()) {
                throw new RuntimeException("Savings API 응답이 비어있습니다.");
            }

            // SavingsApiResultWrapper로 파싱
            ResponseEntity<SavingsApiResultWrapper> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    SavingsApiResultWrapper.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Savings API 응답 파싱 실패. JSON 구조를 확인해주세요.");
            }

            SavingsApiResultWrapper.SavingsApiResult result = response.getBody().getResult();

            if (result == null) {
                throw new RuntimeException("Savings API 결과 데이터가 null입니다.");
            }

            SavingsProductAndOptionListDto dto = new SavingsProductAndOptionListDto();

            dto.setProducts(result.getBaseList().stream()
                    .collect(Collectors.toList()));

            dto.setOptions(result.getOptionList().stream()
                    .collect(Collectors.toList()));

            return dto;

        } catch (ResourceAccessException e) {
            throw new RuntimeException("Savings API 서버에 연결할 수 없습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Savings API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}

package com.banklab.product.service.annuity;

import com.banklab.product.domain.annuity.AnnuityOption;
import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.dto.annuity.AnnuityApiResultWrapper;
import com.banklab.product.dto.annuity.AnnuityOptionDto;
import com.banklab.product.dto.annuity.AnnuityProductAndOptionListDto;
import com.banklab.product.dto.annuity.AnnuityProductDto;
import com.banklab.product.dto.savings.SavingsApiResultWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnuityApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_URL = "https://finlife.fss.or.kr/finlifeapi/annuitySavingProductsSearch.json";

    @Value("${finlife.api-key}")
    private String API_KEY;

    public AnnuityProductAndOptionListDto fetchProductsFromApi() {
        String fullUrl = API_URL + "?auth=" + API_KEY + "&topFinGrpNo=060000&pageNo=1";

        System.out.println("Annuity API 호출 URL: " + fullUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<String> stringResponse = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Annuity API 응답 상태: " + stringResponse.getStatusCode());
            System.out.println("Annuity API 응답 Body: " + stringResponse.getBody());

            if (stringResponse.getBody() == null || stringResponse.getBody().isEmpty()) {
                throw new RuntimeException("Annuity API 응답이 비어있습니다.");
            }

            // AnnuityApiResultWrapper로 파싱
            ResponseEntity<AnnuityApiResultWrapper> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    AnnuityApiResultWrapper.class
            );


            if (response.getBody() == null) {
                throw new RuntimeException("Annuity API 응답 파싱 실패. JSON 구조를 확인해주세요.");
            }

            AnnuityApiResultWrapper.AnnuityApiResult result = response.getBody().getResult();

            if (result == null) {
                throw new RuntimeException("Annuity API 결과 데이터가 null입니다.");
            }

            AnnuityProductAndOptionListDto dto = new AnnuityProductAndOptionListDto();

            dto.setProducts(result.getBaseList().stream()
                    .collect(Collectors.toList()));

            dto.setOptions(result.getOptionList().stream()
                    .collect(Collectors.toList()));

            System.out.println("Annuity API 호출 성공: 상품 " + result.getBaseList().size() + "개, 옵션 " + result.getOptionList().size() + "개");

            return dto;
        } catch (ResourceAccessException e) {
            System.err.println("Annuity API 네트워크 연결 오류: " + e.getMessage());
            throw new RuntimeException("Annuity API 서버에 연결할 수 없습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Annuity API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Annuity API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
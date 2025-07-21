package com.banklab.codeapi.service;

import com.banklab.category.service.CategoryService;
import com.banklab.codeapi.domain.TransactionHistoryVO;
import com.banklab.codeapi.dto.TransactionRequestDto;
import com.banklab.codeapi.mapper.codeapiMapper;
import com.banklab.perplexity.service.PerplexityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class CodeapiService {

    private final codeapiMapper codeapiMapper;
    private final CategoryService categoryService;
    private final PerplexityService perplexityService;
    private final RestTemplate restTemplate;
    private final Map<String, Long> categoryMap;


    private static final String API_HOST = "https://development.codef.io";
    private static final String API_PATH = "/v1/kr/bank/p/account/transaction-list";

    @Value("${codefapi.access.token}")
    private String ACCESS_TOKEN;

    public void fetchAndSaveTransactions(TransactionRequestDto request) {
        String jsonResponse = callTransactionListApi(request);
        try {
            jsonResponse = URLDecoder.decode(jsonResponse, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        List<TransactionHistoryVO> transactionList = parseTransactionList(jsonResponse);

        // 거래 내역 저장
        saveTransactionsToDb(transactionList);
    }

    private String callTransactionListApi(TransactionRequestDto request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + ACCESS_TOKEN);

        HttpEntity<TransactionRequestDto> entity = new HttpEntity<>(request, httpHeaders);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_HOST + API_PATH, entity, String.class);
            return response.getBody();
        }catch (HttpClientErrorException e){
            log.error(e.getStatusCode());
            log.error(e.getResponseBodyAsString());
        }

        return "No data";
    }

    private List<TransactionHistoryVO> parseTransactionList(String json) {
        List<TransactionHistoryVO> transactions = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode dataNode = root.path("data");
            JsonNode list = dataNode.path("resTrHistoryList");

            for (JsonNode item : list) {
                TransactionHistoryVO vo = mapper.treeToValue(item, TransactionHistoryVO.class);
                vo.setResAccount(dataNode.path("resAccount").asText());
                transactions.add(vo);
            }

            // 상호명 리스트 추출
            List<String> descriptions = transactions.stream()
                    .map(TransactionHistoryVO::getDescription)
                    .collect(Collectors.toList());

            // Perplexity API로 전체 상호명 리스트 전달하여 카테고리 분류
            List<String> categoryNames = perplexityService.getCompletions(descriptions);

            // 카테고리 ID 조회 및 설정
            for (int i = 0; i < transactions.size(); i++) {
                String categoryName = categoryNames.get(i).trim();
                Long categoryId = categoryMap.getOrDefault(categoryName, 8L); // Map에서 ID 조회
                transactions.get(i).setCategory_id(categoryId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }
    
    private void saveTransactionsToDb(List<TransactionHistoryVO> transactions) {
        if (!transactions.isEmpty()) {
            codeapiMapper.insertTransactions(transactions);
        }
    }
}

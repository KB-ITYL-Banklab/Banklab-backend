package com.banklab.codeapi.service;

import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.service.CategoryService;
import com.banklab.codeapi.domain.TransactionHistoryVO;
import com.banklab.codeapi.dto.TransactionRequestDto;
import com.banklab.codeapi.mapper.codeapiMapper;
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
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CodeapiService {

    private final codeapiMapper codeapiMapper;
    private final CategoryService categoryService;
    private final RestTemplate restTemplate;

    private static final String API_HOST = "https://development.codef.io";
    private static final String API_PATH = "/v1/kr/bank/p/account/transaction-list";

    @Value("${codefapi.access.token}")
    private String ACCESS_TOKEN;

    public void fetchAndSaveTransactions(TransactionRequestDto request) {
        String jsonResponse = callTransactionListA1pi(request);
         try {
            jsonResponse= URLDecoder.decode(jsonResponse, "UTF-8");
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        List<TransactionHistoryVO> transactionList = parseTransactionList(jsonResponse);
//        saveTransactionsToDb(transactionList);
    }

    /**
     *
     * @param request: 요청 파라미터
     * @return: 거래 내역 리스트
     */
    private String callTransactionListA1pi(TransactionRequestDto request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer "+ACCESS_TOKEN);

        HttpEntity<TransactionRequestDto> entity = new HttpEntity<>(request, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(API_HOST + API_PATH, entity, String.class);
        System.out.println("response: "+response.getBody());

        return response.getBody();
    }


    /**
     * 
     * @param json: 거래 내역 데이터
     * @return
     */
    private List<TransactionHistoryVO> parseTransactionList(String json){
        List<TransactionHistoryVO> transactions = new ArrayList<>();
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root=  mapper.readTree(json);
            JsonNode dataNode = root.path("data");
            JsonNode list= dataNode.path("resTrHistoryList");

            // VO 변환 및 기본 필드 설정
            for(JsonNode item: list){
                TransactionHistoryVO vo = mapper.treeToValue(item, TransactionHistoryVO.class);
                vo.setResAccount(dataNode.path("resAccount").asText());
                transactions.add(vo);
            }

            // 상호명 리스트
            List<String> descriptions = transactions.stream()
                    .map(TransactionHistoryVO::getDescription)
                    .toList();
    
            // Perplexity API 로 batch 분류
            List<String> categories = new ArrayList<>();

            for(int i = 0; i < categories.size(); i++){
                String category = categories.get(i);
                Long id = getCategory(category);
                transactions.get(i).setCategory_id(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * 
     * @param description 카테고리 이름
     * @return  카테고리 아이디
     */
    private Long getCategory(String description){
        CategoryDTO category = categoryService.createCategory(description);
        return category.getId();
    }


    /**
     *
     * @param transactions 거래 내역 리스트
     */
    private void saveTransactionsToDb(List<TransactionHistoryVO> transactions) {
        if (!transactions.isEmpty()) {
            codeapiMapper.insertTransactions(transactions);
        }
    }
}

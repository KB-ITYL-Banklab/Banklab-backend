package com.banklab.risk.service;

import com.banklab.risk.dto.BatchRiskAnalysisRequest;
import com.banklab.risk.dto.RiskAnalysisResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@Slf4j
/**
 * anthropic api를 사용하여 모든 상품의 위험도를 측정합니다
 */
public class BatchClaudeAiAnalysisService {
    
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl = "https://api.anthropic.com/v1/messages";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public BatchClaudeAiAnalysisService(@Value("${ai.claude.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Batch 분석 - 한 번의 API 호출로 여러 상품 분석
     */
    public List<RiskAnalysisResponse> batchAnalyzeProductRisks(List<BatchRiskAnalysisRequest> requests) {
        try {
            // 배치 크기 제한 (Claude 토큰 제한 고려)
            int batchSize = Math.min(requests.size(), 20); // 한 번에 최대 20개
            
            if (requests.size() <= batchSize) {
                return processBatch(requests);
            } else {
                // 큰 배치는 여러 번으로 나누어 처리
                return processLargeBatch(requests, batchSize);
            }
        } catch (Exception e) {
            log.error("배치 AI 위험도 분석 중 오류 발생", e);
            // 실패 시 기본값들 반환
            return requests.stream()
                .map(req -> new RiskAnalysisResponse("MEDIUM", "AI 분석 오류로 인한 기본 평가"))
                .toList();
        }
    }
    
    private List<RiskAnalysisResponse> processLargeBatch(List<BatchRiskAnalysisRequest> requests, int batchSize) {
        return IntStream.range(0, (requests.size() + batchSize - 1) / batchSize)
            .mapToObj(i -> {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, requests.size());
                List<BatchRiskAnalysisRequest> batch = requests.subList(start, end);
                
                try {
                    Thread.sleep(200); // 배치 간 딜레이
                    return processBatch(batch);
                } catch (Exception e) {
                    log.error("배치 {} 처리 실패", i, e);
                    return batch.stream()
                        .map(req -> new RiskAnalysisResponse("MEDIUM", "배치 처리 오류"))
                        .toList();
                }
            })
            .flatMap(List::stream)
            .toList();
    }
    
    private List<RiskAnalysisResponse> processBatch(List<BatchRiskAnalysisRequest> requests) {
        String prompt = buildBatchPrompt(requests);
        String response = callClaudeApi(prompt);
        return parseBatchResponse(response, requests.size());
    }
    
    private String buildBatchPrompt(List<BatchRiskAnalysisRequest> requests) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            당신은 매우 보수적인 금융상품 위험도 분석 전문가입니다. 고객 보호를 최우선으로 하며, 작은 위험 요소라도 중요하게 평가하여
            아래 여러 금융상품들을 한 번에 분석하여 각각의 위험도를 평가해주세요.
            
            ## 매우 보수적인 위험도 분류 기준 (고객 보호 최우선)
            **LOW (저위험)**: 
            - 예금/적금: 기본적인 해당 사항
            - 대출: 해당 없음 
            
            **MEDIUM (중위험)**: 
            - 예금/적금: 다음 중 세 개 이상 해당하면 중위험
              * 우대조건 존재 (추가 거래 요구, 잔액 조건 등)
              * 복합적 가입방법 (온라인 전용, 특정 채널 제한)
              * 특정 대상 제한 (직장인, 연령 제한, 소득 조건 등)
              * 만기 제약 (중도해지 불이익, 자동연장 조건)
              * 금리 변동 요소 (단계별 금리, 조건부 금리)
              * 최소/최대 한도 제약
            - 대출: 해당 없음 
            
            **HIGH (고위험)**: 
            - 예금/적금: 다음 중 하나라도 해당하면 고위험
              * 매우 복잡한 조건부 상품 (다단계 우대조건)
              * 높은 변동성 (시장연동형, 복잡한 금리 구조)
              * 매우 제한적 대상 (까다로운 자격 조건)
              * 심각한 해지 제약 (높은 중도해지 수수료, 장기 구속)
            - 대출: 모든 대출 상품 (특히 고금리, 복잡한 조건, 담보 위험 등)
           
            
            ## 매우 엄격한 평가 원칙
            - **대출 상품은 모두 HIGH 이상**: 개인신용대출도 최소 HIGH (상환 부담, 연체 위험, 신용등급 영향)
            - **예금/적금도 조건이 있으면 즉시 MEDIUM**: 우대조건, 가입 제한, 온라인 전용 등 조건이 세 개 이상 존재하면 MEDIUM
            - **복잡하거나 이해하기 어려운 모든 요소는 위험 요소**: 고객이 완전히 이해하지 못할 수 있는 모든 조건
            - **"간편함"보다 "안전함"을 우선**: 조금이라도 복잡하면 위험으로 간주
            
            ## 분석할 상품들
            """);
        
        for (int i = 0; i < requests.size(); i++) {
            BatchRiskAnalysisRequest req = requests.get(i);
            prompt.append(String.format("""
                
                ### 상품 %d
                - **금융회사**: %s
                - **상품명**: %s  
                - **가입방법**: %s
                - **우대조건**: %s
                - **만기후이자율**: %s
                - **기타사항**: %s
                - **상품타입**: %s
                """, 
                i + 1,
                getString(req.getKorCoNm()),
                getString(req.getFinPrdtNm()),
                getString(req.getJoinWay()),
                getString(req.getSpclCnd()),
                getString(req.getMtrtInt()),
                getString(req.getEtcNote()),
                req.getProductType().name()
            ));
        }
        
        prompt.append("""
            
            ## 응답 형식
          
            각 상품에 대해 아래 JSON 배열 형식으로 순서대로 응답해주세요:
            
            [
              {
                "product_index": 1,
                "risk_level": "LOW|MEDIUM|HIGH",
                "risk_reason": "매우 보수적 관점에서 발견한 위험 요소들을 구체적으로 나열하여 설명"
              },
              {
                "product_index": 2,
                "risk_level": "LOW|MEDIUM|HIGH", 
                "risk_reason": "매우 보수적 관점에서 발견한 위험 요소들을 구체적으로 나열하여 설명"
              }
            ]
            
            JSON 배열만 응답하고 다른 텍스트는 포함하지 마세요.
            """);
        
        return prompt.toString();
    }
    
    private String getString(String value) {
        return value != null ? value : "정보 없음";
    }
    
    private String callClaudeApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");
        
        Map<String, Object> requestBody = Map.of(
            "model", "claude-3-haiku-20240307", // 가장 저렴한 모델
            "max_tokens", 4000, // 배치 처리를 위해 토큰 증가
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
        
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
        return (String) content.get(0).get("text");
    }
    
    private List<RiskAnalysisResponse> parseBatchResponse(String response, int expectedCount) {
        try {
            System.out.println("=== PARSING RESPONSE ===");
            System.out.println("Expected count: " + expectedCount);
            System.out.println("Raw response: " + response);

            String jsonPart = extractJsonFromResponse(response);
            jsonPart = sanitizeJson(jsonPart);
            System.out.println("Extracted JSON: " + jsonPart);
            
            List<Map<String, Object>> results = objectMapper.readValue(
                jsonPart,
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            System.out.println("Parsed results count: " + results.size());
            
            List<RiskAnalysisResponse> responses = results.stream()
                .map(this::mapToRiskAnalysisResponse)
                .toList();
                
            // 개수가 맞지 않으면 부족한 만큼 기본값 추가
            if (responses.size() < expectedCount) {
                List<RiskAnalysisResponse> allResponses = new java.util.ArrayList<>(responses);
                for (int i = responses.size(); i < expectedCount; i++) {
                    allResponses.add(new RiskAnalysisResponse("MEDIUM", "응답 개수 부족으로 인한 기본 평가"));
                }
                return allResponses;
            }
            
            return responses;
                
        } catch (Exception e) {
            log.error("배치 AI 응답 파싱 실패 - 응답: {}", response, e);
            System.out.println("=== PARSING ERROR ===");
            e.printStackTrace();
            // 파싱 실패 시 기본값들 반환
            return IntStream.range(0, expectedCount)
                .mapToObj(i -> new RiskAnalysisResponse("MEDIUM", "응답 파싱 실패로 인한 기본 평가"))
                .toList();
        }
    }
    
    private RiskAnalysisResponse mapToRiskAnalysisResponse(Map<String, Object> result) {
        String riskLevel = (String) result.get("risk_level");
        String riskReason = (String) result.get("risk_reason");
        return new RiskAnalysisResponse(riskLevel, riskReason);
    }
    
    private String extractJsonFromResponse(String response) {
        int start = response.indexOf("[");
        int end = response.lastIndexOf("]") + 1;
        if (start >= 0 && end > start) {
            return response.substring(start, end);
        }
        // 배열이 아닌 경우 객체 찾기
        start = response.indexOf("{");
        end = response.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return "[" + response.substring(start, end) + "]";
        }
        return response;
    }
    private String sanitizeJson(String response) {
        // 1. 앞뒤 공백 및 특수문자 제거
        String sanitized = response.trim();

        // 2. 바깥쪽 큰따옴표로 감싸져 있으면 제거
        if (sanitized.startsWith("\"") && sanitized.endsWith("\"")) {
            sanitized = sanitized.substring(1, sanitized.length() - 1);
        }
        // 3. 이중 이스케이프된 \n, \t 등은 한 번만 이스케이프
        sanitized = sanitized.replaceAll("\\\\n", "\\n");
        sanitized = sanitized.replaceAll("\\\\t", "\\t");
        sanitized = sanitized.replaceAll("\\\\\"", "\"");

        // 4. single quote → double quote (필요시)
        sanitized = sanitized.replace('\'', '\"');

        // 5. 기타 불필요한 백슬래시 제거 (필요시)
        sanitized = sanitized.replaceAll("\\\\", "");

        return sanitized;
    }
}


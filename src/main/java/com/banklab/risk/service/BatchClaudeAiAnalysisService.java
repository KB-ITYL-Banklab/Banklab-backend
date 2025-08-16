package com.banklab.risk.service;

import com.banklab.risk.dto.BatchRiskAnalysisRequest;
import com.banklab.risk.dto.RiskAnalysisResponse;
import com.banklab.risk.domain.RiskLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
    private final ObjectMapper objectMapper;
    
    public BatchClaudeAiAnalysisService(@Value("${ai.claude.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        
        // ObjectMapper 설정을 더 관대하게 구성
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * Batch 분석 - 한 번의 API 호출로 여러 상품 분석
     */
    public List<RiskAnalysisResponse> batchAnalyzeProductRisks(List<BatchRiskAnalysisRequest> requests) {
        log.info("배치 위험도 분석 시작 - 상품 수: {}", requests.size());
        
        try {
            // 배치 크기 제한 (Claude API 안정성 고려) - 5개로 다시 축소
            int batchSize = Math.min(requests.size(), 5);
            
            if (requests.size() <= batchSize) {
                return processBatch(requests);
            } else {
                // 큰 배치는 여러 번으로 나누어 처리
                return processLargeBatch(requests, batchSize);
            }
        } catch (Exception e) {
            log.error("배치 AI 위험도 분석 중 전체 오류 발생 - 상품 수: {}", requests.size(), e);
            // 실패 시 기본값들 반환
            return requests.stream()
                .map(req -> createSafeRiskAnalysisResponse("MEDIUM", "AI 분석 오류로 인한 기본 평가: " + e.getMessage()))
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
                    Thread.sleep(5000); // 배치 간 딜레이 증가 (2000ms -> 5000ms)
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
        log.info("배치 처리 시작 - 요청 수: {}", requests.size());
        try {
            String prompt = buildBatchPrompt(requests);
            log.debug("생성된 프롬프트 길이: {} chars", prompt.length());
            
            String response = callClaudeApi(prompt);
            log.info("Claude API 응답 받음 - 길이: {} chars", response.length());
            
            return parseBatchResponse(response, requests.size());
        } catch (Exception e) {
            log.error("배치 처리 실패 - 요청 수: {}", requests.size(), e);
            return requests.stream()
                .map(req -> createSafeRiskAnalysisResponse("MEDIUM", "배치 처리 오류: " + e.getMessage()))
                .toList();
        }
    }
    
    private String buildBatchPrompt(List<BatchRiskAnalysisRequest> requests) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                ## 기본 설정
                당신은 금융상품 위험도 분석 전문가입니다. 바젤3 규제와 금융감독원 기준을 준수하여 고객 보호를 최우선으로 상품 위험도를 평가해주세요.
                위험도 분류 기준 (3단계 확정)
                
                ## LOW (낮음)
                예금/적금: 기본 정기예적금, 단순 금리조건, 우대조건 1개 이하
                기타: 정부보장 상품, 예금자보호법 완전 적용 상품
                
                ## MEDIUM (보통)
                예금/적금: 복합우대조건(2-4개), 중등도 금리변동성, 가입제한 존재
                연금: 확정급여형, 최소보장수익률 2-4%, 중간 복잡도 구조
                기타: 일반적 위험 수준의 금융상품
                
                ## HIGH (높음)
                대출상품: 모든 유형 (신용/담보/전세자금 포함)
                예금/적금: 고도 복잡 우대조건(5개 이상), 높은 금리변동성
                연금: 변액연금, 최소보장 없음, 고도 복잡 구조
                기타: 원금손실 위험 상품
                
                상품별 정밀 분석 기준
                예금/적금 위험요소 분석
                금리변동성 측정:
                기준금리 연동 여부 (한국은행 기준금리/CD금리 연동시 위험 증가)
                변동금리 구간 폭 (±1%p 이상시 위험 증가)
                단계별 금리 적용 복잡성 (3단계 이상시 위험 증가)
                
                복합우대조건 위험도:
                우대조건 개수: 1개(안전) → 2-3개(보통) → 4개이상(위험)
                조건 달성 난이도: 급여이체(쉬움) → 카드실적(보통) → 복합실적(어려움)
                중도해지 패널티: 없음(안전) → 이자감소(보통) → 원금손실(위험)
                
                대출상품 바젤3 기준 분석
                바젤3 규제 준수 평가:
                LTV/DTI 규제 준수도 (규제 한계선 근접시 위험 증가)
                자본건전성 기준 (BIS 비율 8% 이상 유지 은행 상품 선별)
                신용위험 가중치 (100% 이상 가중치 적용 상품은 고위험)
                
                연체율 기반 위험도:
                신용대출: 업계 평균 연체율 3-5% 기준 평가
                담보대출: 담보가치 하락 위험, LTV 비율별 연체율 차등 평가
                전세자금대출: 전세가격 변동성, 깡통전세 위험도 반영
                
                연금상품 구조 분석
                복합상품 구조 분석:
                
                보장/변액 혼합비율 (변액 비중 높을수록 위험 증가)
                수수료 구조: 단순(1-2종) → 복합(3-4종) → 고도복합(5종이상)
                해지환급금 산정 복잡성 (복잡한 계산식일수록 위험 증가)
                
                장기 리스크 평가:
                인플레이션 위험: 명목수익률 vs 실질수익률 격차 분석
                장기수익률 변동성: 과거 10년 데이터 기반 변동성 측정
                조기해지 손실률: 가입기간별 해지손실 구간 평가 (10년미만 고위험)
                
                ## 종합 위험도 판정 로직
                가중치 적용
                대출상품: 무조건 HIGH (바젤3 기본 위험 분류)
                
                예적금:금리변동성 (40%) + 복합우대조건 (60%) = 종합점수
                70점 이상: HIGH, 40-69점: MEDIUM, 39점 이하: LOW
                
                연금상품:복합구조 (50%) + 장기리스크 (50%) = 종합점수
                75점 이상: HIGH, 45-74점: MEDIUM, 44점 이하: LOW
                
                추가 위험요소
                금융회사 건전성: 경영평가 하위권 회사 상품은 위험도 +1단계
                상품 복잡성: 설명서 10페이지 초과시 위험도 증가
                중도해지 제약: 높은 패널티나 제약시 위험도 증가
            
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
            JSON 배열로만 응답하세요:
            
            [
              {
                "product_index": 1,
                "risk_level": "LOW|MEDIUM|HIGH",
                "risk_reason": "구체적 위험요소와 판정근거 설명 (바젤3/금리변동성/복합조건 등 명시)"
              }
            ]
            
            중요: 다른 텍스트 없이 JSON만 응답하세요.
            """);
        
        return prompt.toString();
    }
    
    private String getString(String value) {
        return value != null ? value : "정보 없음";
    }
    
    private String callClaudeApi(String prompt) {
        int maxRetries = 5;
        long baseDelay = 3000; // 3초
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-api-key", apiKey);
                headers.set("anthropic-version", "2023-06-01");
                
                Map<String, Object> requestBody = Map.of(
                    "model", "claude-3-haiku-20240307",
                    "max_tokens", 4000,
                    "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                    )
                );
                
                log.debug("Claude API 호출 시작 - 시도 {}/{}", attempt, maxRetries);
                
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                
                if (response.getStatusCode() != HttpStatus.OK) {
                    throw new RuntimeException("Claude API 호출 실패 - HTTP 상태: " + response.getStatusCode());
                }
                
                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null) {
                    throw new RuntimeException("Claude API 응답 본문이 null입니다");
                }
                
                List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
                if (content == null || content.isEmpty()) {
                    throw new RuntimeException("Claude API 응답에 content가 없습니다");
                }
                
                String responseText = (String) content.get(0).get("text");
                log.info("Claude API 호출 성공 - 응답 길이: {} chars (시도 {})", 
                        responseText != null ? responseText.length() : 0, attempt);
                
                return responseText;
                
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                log.warn("Claude API 서버 오류 - 시도 {}/{}: {}", attempt, maxRetries, e.getMessage());
                
                if (e.getRawStatusCode() == 529) { // Overloaded
                    if (attempt < maxRetries) {
                        long delay = baseDelay * attempt; // 지수 백오프
                        log.info("API 과부하로 {}ms 대기 후 재시도...", delay);
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                        }
                        continue;
                    }
                }
                throw new RuntimeException("Claude API 호출 실패 (서버 오류)", e);
                
            } catch (Exception e) {
                log.error("Claude API 호출 중 오류 발생 - 시도 {}/{}", attempt, maxRetries, e);
                
                if (attempt < maxRetries) {
                    long delay = baseDelay * attempt;
                    log.info("{}ms 대기 후 재시도...", delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                    continue;
                }
                throw new RuntimeException("Claude API 호출 실패", e);
            }
        }
        
        throw new RuntimeException("Claude API 호출 최대 재시도 횟수 초과");
    }
    
    private List<RiskAnalysisResponse> parseBatchResponse(String response, int expectedCount) {
        try {
            log.info("=== PARSING RESPONSE ===");
            log.info("Expected count: {}", expectedCount);
            log.info("Raw response length: {} chars", response.length());
            log.debug("Raw response preview: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);

            String jsonPart = extractJsonFromResponse(response);
            log.info("Extracted JSON length: {} chars", jsonPart.length());
            
            // JSON이 이미 올바른 형태인지 먼저 확인
            List<Map<String, Object>> results;
            try {
                // 첫 번째 시도: 원본 JSON 그대로 파싱
                results = objectMapper.readValue(
                    jsonPart,
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                log.info("JSON parsing successful (원본) - parsed {} results", results.size());
                
            } catch (Exception firstParseError) {
                log.warn("원본 JSON 파싱 실패, 정제 후 재시도: {}", firstParseError.getMessage());
                
                try {
                    // 두 번째 시도: 정제 후 파싱
                    String sanitizedJson = sanitizeJson(jsonPart);
                    log.info("Sanitized JSON length: {} chars", sanitizedJson.length());
                    log.debug("Sanitized JSON preview: {}", sanitizedJson.length() > 1000 ? sanitizedJson.substring(0, 1000) + "..." : sanitizedJson);
                    
                    results = objectMapper.readValue(
                        sanitizedJson,
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    log.info("JSON parsing successful (정제 후) - parsed {} results", results.size());
                    
                } catch (Exception secondParseError) {
                    log.error("정제 후에도 JSON 파싱 실패", secondParseError);
                    log.error("Failed JSON content: {}", jsonPart);
                    
                    // 파싱 완전 실패 - 빈 배열로 처리
                    results = new ArrayList<>();
                }
            }
            
            List<RiskAnalysisResponse> responses = new ArrayList<>();
            
            for (int i = 0; i < results.size(); i++) {
                try {
                    Map<String, Object> result = results.get(i);
                    RiskAnalysisResponse mapped = mapToRiskAnalysisResponse(result);
                    responses.add(mapped);
                    log.debug("Successfully mapped result #{}: {}", i + 1, mapped.getRiskLevel());
                } catch (Exception e) {
                    log.warn("Failed to map result #{}: {}", i + 1, results.get(i), e);
                    // 매핑 실패 시 기본값 추가
                    responses.add(createSafeRiskAnalysisResponse("MEDIUM", "응답 매핑 실패로 인한 기본 평가"));
                }
            }
                
            // 개수가 맞지 않으면 부족한 만큼 기본값 추가
            while (responses.size() < expectedCount) {
                responses.add(createSafeRiskAnalysisResponse("MEDIUM", "응답 개수 부족으로 인한 기본 평가"));
                log.warn("Added default response for missing result #{}", responses.size());
            }
            
            log.info("Final response count: {} (expected: {})", responses.size(), expectedCount);
            return responses;
                
        } catch (Exception e) {
            log.error("전체 배치 AI 응답 파싱 실패", e);
            log.error("Response that failed: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);
            
            // 파싱 실패 시 모든 항목에 대해 기본값들 반환
            List<RiskAnalysisResponse> fallbackResponses = new ArrayList<>();
            for (int i = 0; i < expectedCount; i++) {
                fallbackResponses.add(createSafeRiskAnalysisResponse("MEDIUM", "응답 파싱 실패로 인한 기본 평가: " + e.getMessage()));
            }
            return fallbackResponses;
        }
    }
    
    private RiskAnalysisResponse mapToRiskAnalysisResponse(Map<String, Object> result) {
        try {
            String riskLevel = (String) result.get("risk_level");
            String riskReason = (String) result.get("risk_reason");
            return createSafeRiskAnalysisResponse(riskLevel, riskReason);
        } catch (Exception e) {
            log.warn("개별 위험도 응답 매핑 실패: {}", result, e);
            return createSafeRiskAnalysisResponse("MEDIUM", "개별 응답 매핑 실패로 인한 기본 평가");
        }
    }
    
    private RiskAnalysisResponse createSafeRiskAnalysisResponse(String riskLevel, String riskReason) {
        try {
            // 유효한 위험도 레벨인지 검증
            if (riskLevel == null || riskLevel.trim().isEmpty()) {
                riskLevel = "MEDIUM";
            }
            
            riskLevel = riskLevel.trim().toUpperCase();
            
            // 유효한 RiskLevel enum 값인지 검증
            RiskLevel.valueOf(riskLevel);
            
            return new RiskAnalysisResponse(riskLevel, riskReason != null ? riskReason : "위험도 평가");
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 위험도 레벨: {}. MEDIUM으로 기본값 설정", riskLevel);
            return new RiskAnalysisResponse("MEDIUM", riskReason != null ? riskReason : "유효하지 않은 위험도로 인한 기본 평가");
        }
    }
    
    private String extractJsonFromResponse(String response) {
        try {
            log.debug("원본 응답에서 JSON 추출 시작 - 길이: {} chars", response.length());
            
            // 마크다운 코드 블록 제거
            if (response.contains("```json")) {
                int start = response.indexOf("```json") + 7;
                int end = response.indexOf("```", start);
                if (end > start) {
                    response = response.substring(start, end).trim();
                    log.debug("```json 블록에서 추출: {} chars", response.length());
                }
            } else if (response.contains("```")) {
                int start = response.indexOf("```") + 3;
                int end = response.indexOf("```", start);
                if (end > start) {
                    response = response.substring(start, end).trim();
                    log.debug("``` 블록에서 추출: {} chars", response.length());
                }
            }
            
            // JSON 배열 추출 시도
            int start = response.indexOf("[");
            int end = response.lastIndexOf("]") + 1;
            if (start >= 0 && end > start) {
                String jsonArray = response.substring(start, end);
                log.debug("JSON 배열 추출 성공: {} chars", jsonArray.length());
                return jsonArray;
            }
            
            // 배열이 아닌 경우 객체들을 찾아서 수동으로 배열 구성
            List<String> jsonObjects = new ArrayList<>();
            int objectStart = 0;
            int objectCount = 0;
            
            while (objectStart < response.length() && objectCount < 50) { // 최대 50개 객체까지만
                objectStart = response.indexOf("{", objectStart);
                if (objectStart == -1) break;
                
                int braceCount = 1;
                int pos = objectStart + 1;
                
                while (pos < response.length() && braceCount > 0) {
                    char c = response.charAt(pos);
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                    } else if (c == '"') {
                        // 문자열 내부의 중괄호는 무시
                        pos++;
                        while (pos < response.length() && response.charAt(pos) != '"') {
                            if (response.charAt(pos) == '\\') {
                                pos++; // 이스케이프 문자 건너뛰기
                            }
                            pos++;
                        }
                    }
                    pos++;
                }
                
                if (braceCount == 0) {
                    String jsonObject = response.substring(objectStart, pos);
                    // 기본적인 JSON 객체 유효성 검사
                    if (jsonObject.contains("product_index") && jsonObject.contains("risk_level")) {
                        jsonObjects.add(jsonObject);
                        objectCount++;
                        log.debug("JSON 객체 #{} 추출: {} chars", objectCount, jsonObject.length());
                    }
                    objectStart = pos;
                } else {
                    break; // 불완전한 객체
                }
            }
            
            if (!jsonObjects.isEmpty()) {
                String result = "[" + String.join(",", jsonObjects) + "]";
                log.debug("수동으로 {} 개 객체를 배열로 구성: {} chars", jsonObjects.size(), result.length());
                return result;
            }
            
            log.warn("JSON을 찾을 수 없음 - 원본 응답: {}", response.substring(0, Math.min(200, response.length())));
            return "[]"; // 빈 배열 반환
            
        } catch (Exception e) {
            log.error("JSON 추출 중 오류 발생 - 응답: {}", response.substring(0, Math.min(200, response.length())), e);
            return "[]";
        }
    }
    private String sanitizeJson(String response) {
        try {
            // 1. 앞뒤 공백 제거
            String sanitized = response.trim();

            // 2. 바깥쪽 큰따옴표로 감싸져 있으면 제거
            if (sanitized.startsWith("\"") && sanitized.endsWith("\"") && sanitized.length() > 1) {
                sanitized = sanitized.substring(1, sanitized.length() - 1);
            }
            
            // 3. JSON이 이미 올바른 형태인지 확인 - 간단한 유효성 검사
            if (sanitized.startsWith("[") && sanitized.endsWith("]")) {
                // 이미 올바른 JSON 배열 형태라면 최소한의 처리만
                log.debug("JSON이 이미 올바른 형태입니다 - 최소 처리만 수행");
                
                // 위험한 제어 문자만 제거 (ASCII 0-8, 11-12, 14-31, 127)
                // 단, 일반적인 줄바꿈(\n=10), 캐리지리턴(\r=13), 탭(\t=9)은 유지
                sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
                
                return sanitized;
            }
            
            // 4. JSON이 손상된 경우에만 복구 시도
            log.debug("JSON 복구 시도 중...");
            
            // 이중 이스케이프된 문자들 복구
            sanitized = sanitized.replaceAll("\\\\\\\\n", "\\\\n");
            sanitized = sanitized.replaceAll("\\\\\\\\t", "\\\\t");
            sanitized = sanitized.replaceAll("\\\\\\\\r", "\\\\r");
            sanitized = sanitized.replaceAll("\\\\\\\\\"", "\\\\\"");
            
            // 위험한 제어 문자 제거
            sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
            
            log.debug("JSON 복구 완료 - 길이: {}", sanitized.length());
            
            return sanitized;
            
        } catch (Exception e) {
            log.error("JSON 정제 중 오류 발생 - 원본 반환", e);
            // 정제 실패 시 원본 그대로 반환
            return response.trim();
        }
    }
}


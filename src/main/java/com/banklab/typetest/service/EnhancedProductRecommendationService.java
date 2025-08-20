package com.banklab.typetest.service;

import com.banklab.typetest.domain.*;
import com.banklab.typetest.domain.enums.*;
import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.product.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 투자 프로필을 기반으로 한 맞춤형 상품 추천 서비스
 * 기존 투자유형별 추천에서 사용자의 상세 프로필과 제약조건을 추가 고려
 * AI를 활용한 최적 4개 상품 선별
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedProductRecommendationService {

    private final ProductRecommendationService fallbackRecommendationService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.claude.api-key:}")
    private String apiKey;

    private final String apiUrl = "https://api.anthropic.com/v1/messages";

    /**
     * 메인 추천 메서드: 기존 추천 상품에서 사용자 프로필 기반 Best 4 선별
     *
     * @param investmentTypeId 투자 유형 ID
     * @param constraints 사용자 제약조건 리스트
     * @param userProfile 사용자 투자 프로필
     * @return 최적화된 추천 상품 리스트
     */
    public List<RecommendedProductDTO> getFilteredRecommendedProducts(
            Long investmentTypeId,
            List<ConstraintType> constraints,
            UserInvestmentProfile userProfile) {

        try {

            // 기존 추천 시스템에서 상품 목록 가져오기
            List<RecommendedProductDTO> baseRecommendations = fallbackRecommendationService.getRecommendedProducts(investmentTypeId);

            // 하드 제약조건 적용 (절대적 필터링)
            List<RecommendedProductDTO> constraintFiltered = applyConstraintsToRecommendations(baseRecommendations, constraints);

            // 4개 이하일 경우 그대로 반환
            if (constraintFiltered.size() <= 4) {
                return constraintFiltered;
            }

            // AI 기반 최적 4개 선별 (API 키가 있을 때만)
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                return selectBest4WithAI(constraintFiltered, userProfile, constraints, investmentTypeId);
            } else {
                // AI 없을 때는 규칙 기반 선별
                return selectBest4WithRules(constraintFiltered, userProfile, constraints);
            }

        } catch (Exception e) {
            return fallbackRecommendationService.getRecommendedProducts(investmentTypeId);
        }
    }

    /**
     * 하드 제약조건 적용 (절대적 필터링)
     *
     * @param recommendations 추천 상품 리스트
     * @param constraints 사용자 제약조건 리스트
     * @return 필터링된 추천 상품 리스트
     */
    private List<RecommendedProductDTO> applyConstraintsToRecommendations(
            List<RecommendedProductDTO> recommendations,
            List<ConstraintType> constraints) {

        if (constraints == null || constraints.isEmpty()) {
            return recommendations;
        }

        List<RecommendedProductDTO> filtered = recommendations.stream()
                .filter(product -> {
                    // 고위험 상품 제외 조건
                    if (constraints.contains(ConstraintType.HIGH_RISK_FORBIDDEN) &&
                        "HIGH".equals(product.getRiskLevel())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return filtered;
    }

    /**
     * AI 기반 최적 4개 선별
     *
     * @param filteredProducts 필터링된 추천 상품 리스트
     * @param userProfile 사용자 투자 프로필
     * @param constraints 사용자 제약조건 리스트
     * @param investmentTypeId 투자 유형 ID
     * @return 최적화된 추천 상품 리스트
     */
    private List<RecommendedProductDTO> selectBest4WithAI(
            List<RecommendedProductDTO> filteredProducts,
            UserInvestmentProfile userProfile,
            List<ConstraintType> constraints,
            Long investmentTypeId) {

        try {
            String aiPrompt = generateAIPrompt(filteredProducts, userProfile, constraints, investmentTypeId);
            String response = callClaudeApi(aiPrompt);
            List<Integer> selectedIndices = parseAIResponse(response);

            // AI가 선택한 인덱스에 해당하는 상품들 반환
            List<RecommendedProductDTO> finalRecommendations = new ArrayList<>();
            for (Integer index : selectedIndices) {
                if (index >= 0 && index < filteredProducts.size()) {
                    finalRecommendations.add(filteredProducts.get(index));
                }
            }
            return finalRecommendations.isEmpty() ?
                selectBest4WithRules(filteredProducts, userProfile, constraints) :
                finalRecommendations;

        } catch (Exception e) {
            log.error("AI 추천 호출 실패, 규칙 기반으로 fallback", e);
            return selectBest4WithRules(filteredProducts, userProfile, constraints);
        }
    }

    /**
     * 규칙 기반 최적 4개 선별 (AI 대안)
     *
     * @param filteredProducts 필터링된 추천 상품 리스트
     * @param userProfile 사용자 투자 프로필
     * @param constraints 사용자 제약조건 리스트
     * @return 최적화된 추천 상품 리스트
     */
    private List<RecommendedProductDTO> selectBest4WithRules(
            List<RecommendedProductDTO> filteredProducts,
            UserInvestmentProfile userProfile,
            List<ConstraintType> constraints) {

        // 사용자 프로필에 따른 점수 계산
        List<ProductScore> scoredProducts = filteredProducts.stream()
                .map(product -> new ProductScore(product, calculateScore(product, userProfile, constraints)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());

        // 상위 4개 선택
        List<RecommendedProductDTO> selectedProducts = scoredProducts.stream()
                .limit(4)
                .map(ProductScore::getProduct)
                .collect(Collectors.toList());

        log.info("규칙 기반 선별 완료: {} 개 상품", selectedProducts.size());
        return selectedProducts;
    }

    /**
     * 사용자 프로필 기반 상품 점수 계산
     */
    private double calculateScore(RecommendedProductDTO product, UserInvestmentProfile profile, List<ConstraintType> constraints) {
        double score = 50.0; // 기본 점수

        if (profile == null) return score;

        // 위험도 매칭 : 점수 조정
        if (product.getRiskLevel() != null) {
            if (profile.getLossToleranceRange() == RiskRange.LOW_RISK) {
                if ("LOW".equals(product.getRiskLevel().name())) score += 20;
                else if ("MEDIUM".equals(product.getRiskLevel().name())) score += 10;
                else score -= 10; // HIGH 위험
            } else {
                if ("HIGH".equals(product.getRiskLevel().name())) score += 15;
                else if ("MEDIUM".equals(product.getRiskLevel().name())) score += 20;
                else score += 10; // LOW 위험도 괜찮음
            }
        }

        // 투자 우선순위 매칭
        if (profile.getPriority() == Priority.SAFETY) {
            if (product.getRiskLevel() != null && "LOW".equals(product.getRiskLevel().name())) score += 15;
            if (product.getProductType() == ProductType.DEPOSIT) score += 10;
            if (product.getProductType() == ProductType.MORTGAGE || product.getProductType() == ProductType.RENTHOUSE) score += 5; // 주택담보/전세자금대출도 안전 우선
        } else if (profile.getPriority() == Priority.RETURN) {
            if (product.getRiskLevel() != null && !"LOW".equals(product.getRiskLevel().name())) score += 10;
        }

        // 투자 방식 매칭
        if (profile.getInvestmentStyle() == InvestmentStyle.REGULAR) {
            if (product.getProductType() == ProductType.SAVINGS) score += 10;
        } else if (profile.getInvestmentStyle() == InvestmentStyle.LUMP_SUM) {
            if (product.getProductType() == ProductType.DEPOSIT) score += 10;
        }

        // 투자 기간 매칭
        if (profile.getInvestmentPeriodRange() == PeriodRange.SHORT_TERM) {
            if (product.getProductType() == ProductType.DEPOSIT) score += 5;
            if (product.getProductType() == ProductType.RENTHOUSE) score += 5; // 전세자금대출 단기 선호
        } else {
            if (product.getProductType() == ProductType.SAVINGS) score += 5;
            if (product.getProductType() == ProductType.MORTGAGE) score += 5; // 주택담보대출 장기 선호
        }

        return score;
    }

    /**
     * AI 프롬프트 생성
     */
    private String generateAIPrompt(
            List<RecommendedProductDTO> products,
            UserInvestmentProfile profile,
            List<ConstraintType> constraints,
            Long investmentTypeId) {

        StringBuilder prompt = new StringBuilder();

        String investmentTypeName = getInvestmentTypeName(investmentTypeId);

        prompt.append(String.format("""
            당신은 전문 금융상품 추천 AI입니다. 
            
            사용자의 투자성향(%s)에 맞게 이미 필터링된 상품들 중에서,
            사용자의 **상세 프로필**을 추가로 고려하여 **가장 적합한 4개**를 선별해주세요.
            
            ## 📊 사용자 프로필
            - **투자 성향**: %s
            - **투자 가능 금액**: %s
            - **목표 수익률**: %s  
            - **투자 기간**: %s
            - **손실 감수 한도**: %s
            - **투자 방식**: %s
            - **우선순위**: %s
            - **제약조건**: %s
            
            ## 📋 상품 목록 (%d개)
            """,
            investmentTypeName,
            investmentTypeName,
            getAmountRangeDescription(profile != null ? profile.getAvailableAmountRange() : null),
            getReturnRangeDescription(profile != null ? profile.getTargetReturnRange() : null),
            getPeriodRangeDescription(profile != null ? profile.getInvestmentPeriodRange() : null),
            getRiskRangeDescription(profile != null ? profile.getLossToleranceRange() : null),
            getInvestmentStyleDescription(profile != null ? profile.getInvestmentStyle() : null),
            getPriorityDescription(profile != null ? profile.getPriority() : null),
            getConstraintsDescription(constraints),
            products.size()
        ));

        for (int i = 0; i < products.size(); i++) {
            RecommendedProductDTO product = products.get(i);
            prompt.append(String.format("""
                
                ### 상품 %d
                - 회사: %s
                - 상품명: %s
                - 분류: %s
                - 위험도: %s
                - 금리: %s
                """,
                i,
                product.getCompanyName(),
                product.getProductName(),
                getProductTypeDescription(product.getProductType() != null ? product.getProductType().name() : "UNKNOWN"),
                getRiskLevelBadge(product.getRiskLevel() != null ? product.getRiskLevel().name() : "MEDIUM"),
                product.getInterestRate() != null ? product.getInterestRate() : "정보없음"
            ));
        }

        prompt.append("""
            
            ## 선별 요청
            
            사용자 프로필을 종합적으로 고려하여 **가장 적합한 4개 상품의 인덱스**를 선별해주세요.
            
            **응답 형식:**
            [인덱스1, 인덱스2, 인덱스3, 인덱스4]
            
            예시: [0, 2, 5, 8]
            
            **중요**: JSON 배열 형태로만 응답하세요.
            """);

        return prompt.toString();
    }

    private String getAmountRangeDescription(AmountRange range) {
        if (range == null) return "정보 없음";
        return switch (range) {
            case UNDER_500 -> "500만원 미만";
            case OVER_500 -> "500만원 이상";
        };
    }

    private String getReturnRangeDescription(ReturnRange range) {
        if (range == null) return "정보 없음";
        return switch (range) {
            case UNDER_3 -> "연 3% 이하";
            case OVER_3 -> "연 3% 초과";
        };
    }

    private String getPeriodRangeDescription(PeriodRange range) {
        if (range == null) return "정보 없음";
        return switch (range) {
            case SHORT_TERM -> "1년 이하 단기";
            case LONG_TERM -> "1년 초과 장기";
        };
    }

    private String getRiskRangeDescription(RiskRange range) {
        if (range == null) return "정보 없음";
        return switch (range) {
            case LOW_RISK -> "5% 이하 손실만 감수";
            case HIGH_RISK -> "5% 초과 손실 감수 가능";
        };
    }

    private String getInvestmentStyleDescription(InvestmentStyle style) {
        if (style == null) return "정보 없음";
        return switch (style) {
            case LUMP_SUM -> "일시납";
            case REGULAR -> "적립식";
        };
    }

    private String getPriorityDescription(Priority priority) {
        if (priority == null) return "정보 없음";
        return switch (priority) {
            case SAFETY -> "안전성 우선";
            case RETURN -> "수익성 우선";
        };
    }

    private String getConstraintsDescription(List<ConstraintType> constraints) {
        if (constraints == null || constraints.isEmpty()) return "특별한 제약 없음";

        List<String> descriptions = constraints.stream()
                .map(constraint -> switch (constraint) {
                    case PRINCIPAL_GUARANTEE -> "원금보장 필수";
                    case HIGH_RISK_FORBIDDEN -> "고위험 상품 금지";
                    case LIQUIDITY_REQUIRED -> "유동성 필수";
                })
                .toList();

        return String.join(", ", descriptions);
    }

    private String getProductTypeDescription(String productType) {
        return switch (productType) {
            case "DEPOSIT" -> "정기예금";
            case "SAVINGS" -> "적금";
            case "CREDITLOAN", "MORTGAGE", "RENTHOUSE" -> "대출";
            case "ANNUITY" -> "연금";
            default -> "기타";
        };
    }

    private String getRiskLevelBadge(String riskLevel) {
        return switch (riskLevel) {
            case "LOW" -> "저위험";
            case "MEDIUM" -> "중위험";
            case "HIGH" -> "고위험";
            default -> "미분류";
        };
    }

    private String getInvestmentTypeName(Long investmentTypeId) {
        switch (investmentTypeId.intValue()) {
            case 1: return "안전형";
            case 2: return "중립형";
            case 3: return "공격형";
            default: return "안전형";
        }
    }

    /**
     * Claude API 호출
     */
    private String callClaudeApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> requestBody = Map.of(
            "model", "claude-3-haiku-20240307",
            "max_tokens", 1000,
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

    /**
     * AI 응답 파싱
     */
    private List<Integer> parseAIResponse(String response) {
        try {
            String jsonPart = extractJsonFromResponse(response);
            List<Integer> indices = objectMapper.readValue(jsonPart, new TypeReference<List<Integer>>() {});

            return indices.stream().limit(4).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", response, e);
            return List.of(0, 1, 2, 3);
        }
    }

    private String extractJsonFromResponse(String response) {
        int start = response.indexOf("[");
        int end = response.lastIndexOf("]") + 1;
        if (start >= 0 && end > start) {
            return response.substring(start, end);
        }
        return "[0, 1, 2, 3]";
    }

    /**
     * 상품 점수 매핑 클래스
     */
    private static class ProductScore {
        final RecommendedProductDTO product;
        final double score;

        ProductScore(RecommendedProductDTO product, double score) {
            this.product = product;
            this.score = score;
        }

        RecommendedProductDTO getProduct() {
            return product;
        }
    }
}

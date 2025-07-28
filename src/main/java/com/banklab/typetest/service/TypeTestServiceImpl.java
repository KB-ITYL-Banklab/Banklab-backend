package com.banklab.typetest.service;

import com.banklab.typetest.domain.*;
import com.banklab.typetest.domain.enums.*;
import com.banklab.typetest.dto.AnswerDTO;
import com.banklab.typetest.dto.RecommendedProductDTO;
import com.banklab.typetest.dto.TypeTestResultDTO;
import com.banklab.typetest.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeTestServiceImpl implements TypeTestService {

    private final QuestionMapper questionMapper;
    private final QuestionChoiceScoreMapper questionChoiceScoreMapper;
    private final InvestmentTypeMapper investmentTypeMapper;
    private final UserInvestmentTypeMapper userInvestmentTypeMapper;
    private final UserInvestmentProfileMapper userInvestmentProfileMapper;
    private final UserInvestmentConstraintsMapper userInvestmentConstraintsMapper;
    private final ProductRecommendationService productRecommendationService;
    private final EnhancedProductRecommendationService enhancedProductRecommendationService;

    /**
     * 질문지 전체 조회
     * @return 질문지 목록 반환
     */
    @Override
    public List<Question> getAllQuestions() {
        return questionMapper.getAllQuestions();
    }

    /**
     * 사용자의 유형검사 결과 제출
     * @param payload 사용자의 답변 데이터
     * @param memberId JWT에서 추출한 회원 ID
     * @return 유형검사 결과 DTO
     */
    @Override
    public TypeTestResultDTO submitAnswersWithMemberId(Map<String, Object> payload, Long memberId) {
        try {
            // 사용자의 답변을 파싱하여 처리
            List<AnswerDTO> answers = parseAnswers(payload);

            // 답변을 질문 타입별로 분류
            Map<QuestionType, List<AnswerDTO>> categorizedAnswers = categorizeAnswers(answers);

            // 제약조건 분석
            List<ConstraintType> constraints = analyzeConstraints(categorizedAnswers.get(QuestionType.CONSTRAINT));

            // 투자 유형 계산
            Long bestTypeId = calculateBestInvestmentTypeWithConstraints(
                categorizedAnswers.get(QuestionType.PERSONALITY),
                constraints
            );

            // 사용자 프로필 생성
            UserInvestmentProfile userProfile = createUserProfile(
                categorizedAnswers.get(QuestionType.DETAIL),
                categorizedAnswers.get(QuestionType.PREFERENCE)
            );

            // 사용자 데이터 저장
            saveUserData(memberId, bestTypeId, userProfile, constraints);

            // 투자 유형 정보 반환
            InvestmentType investmentType = getInvestmentType(bestTypeId);

            return TypeTestResultDTO.builder()
                .userId(memberId)
                .investmentTypeId(investmentType.getId())
                .investmentTypeName(investmentType.getInvestmentTypeName())
                .message("유형검사 답변 저장 및 투자유형 계산 완료")
                .build();
        } catch (Exception e) {
            log.error("유형검사 답변 저장 중 오류", e);
            return createFailResult("유형검사 답변 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 투자 성향 결과가 있는지 판별
     * 결과가 있다면, 해당 유저의 투자 성향과 추천된 상품 4개를 반환
     * @param userId 사용자 ID
     * @return 유형검사 결과 DTO
     */
    @Override
    public TypeTestResultDTO getTestResultByUserId(Long userId) {
        try {
            // 사용자 투자 유형 조회
            UserInvestmentType userInvestmentType = userInvestmentTypeMapper.findByUserId(userId);
            if (userInvestmentType == null) {
                return createFailResult("해당 사용자의 투자성향 테스트 결과를 찾을 수 없습니다.");
            }

            // 투자 유형 정보 조회
            InvestmentType investmentType = getInvestmentType(userInvestmentType.getInvestmentTypeId());

            // 추천 상품 조회
            List<ConstraintType> constraints = getUserConstraints(userId);
            UserInvestmentProfile userProfile = userInvestmentProfileMapper.findByUserId(userId);
            List<RecommendedProductDTO> recommendedProducts = getFilteredRecommendedProducts(
                userInvestmentType.getInvestmentTypeId(), constraints, userProfile);

            // 추천 상품이 4개를 초과하면 4개로 제한
            if (recommendedProducts != null && recommendedProducts.size() > 4) {
                recommendedProducts = recommendedProducts.subList(0, 4);
            }

            return createSuccessResult(userId, investmentType, recommendedProducts);
        } catch (Exception e) {
            log.error("사용자 테스트 결과 조회 중 오류 발생: userId={}", userId, e);
            return createFailResult("테스트 결과 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 투자 성향에 맞는 전체 상품 보기
     * @param userId 사용자 ID
     * @return 유형검사 결과 DTO
     */
    @Override
    public TypeTestResultDTO getAllProductsByType(Long userId) {
        try {
            // 사용자 투자 유형 조회
            UserInvestmentType userInvestmentType = userInvestmentTypeMapper.findByUserId(userId);
            if (userInvestmentType == null) {
                return createFailResult("해당 사용자의 투자성향 테스트 결과를 찾을 수 없습니다.");
            }

            // 투자 유형 정보 조회
            InvestmentType investmentType = getInvestmentType(userInvestmentType.getInvestmentTypeId());

            // 전체 추천 상품 조회
            List<RecommendedProductDTO> allProducts = productRecommendationService.getRecommendedProducts(
                userInvestmentType.getInvestmentTypeId()
            );

            return TypeTestResultDTO.builder()
                .userId(userId)
                .investmentTypeId(investmentType.getId())
                .investmentTypeName(investmentType.getInvestmentTypeName())
                .investmentTypeDesc(investmentType.getInvestmentTypeDesc())
                .recommendedProducts(allProducts)
                .message("전체 상품 조회 완료")
                .build();
        } catch (Exception e) {
            log.error("전체 상품 조회 중 오류 발생: userId={}", userId, e);
            return createFailResult("전체 상품 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 응답을 질문 타입별로 분류
     */
    private Map<QuestionType, List<AnswerDTO>> categorizeAnswers(List<AnswerDTO> answers) {
        Map<QuestionType, List<AnswerDTO>> categorized = new HashMap<>();
        
        for (AnswerDTO answer : answers) {
            Question question = questionMapper.findById(answer.getQuestionId());
            if (question != null) {
                categorized.computeIfAbsent(question.getQuestionType(), k -> new ArrayList<>()).add(answer);
            }
        }
        
        return categorized;
    }
    
    /**
     * 제약조건 분석 (하드필터링 조건)
     */
    private List<ConstraintType> analyzeConstraints(List<AnswerDTO> constraintAnswers) {
        List<ConstraintType> constraints = new ArrayList<>();
        
        if (constraintAnswers == null) return constraints;
        
        for (AnswerDTO answer : constraintAnswers) {
            switch (answer.getQuestionId().intValue()) {
                case 5: // 원금 손실 위험
                    if (answer.getChoice() == ChoiceType.A) {
                        constraints.add(ConstraintType.PRINCIPAL_GUARANTEE);
                    }
                    break;
                case 6: // 고위험 상품
                    if (answer.getChoice() == ChoiceType.A) {
                        constraints.add(ConstraintType.HIGH_RISK_FORBIDDEN);
                    }
                    break;
                case 7: // 중도해지 수수료
                    if (answer.getChoice() == ChoiceType.A) {
                        constraints.add(ConstraintType.LIQUIDITY_REQUIRED);
                    }
                    break;
            }
        }
        
        return constraints;
    }
    
    /**
     * 사용자 프로필 생성
     */
    private UserInvestmentProfile createUserProfile(List<AnswerDTO> detailAnswers, List<AnswerDTO> preferenceAnswers) {
        UserInvestmentProfile.UserInvestmentProfileBuilder builder = UserInvestmentProfile.builder();
        
        // DETAIL 응답 처리
        if (detailAnswers != null) {
            for (AnswerDTO answer : detailAnswers) {
                switch (answer.getQuestionId().intValue()) {
                    case 8: // 투자 가능 금액
                        builder.availableAmountRange(answer.getChoice() == ChoiceType.A ? AmountRange.UNDER_500 : AmountRange.OVER_500);
                        break;
                    case 9: // 희망 수익률
                        builder.targetReturnRange(answer.getChoice() == ChoiceType.A ? ReturnRange.UNDER_3 : ReturnRange.OVER_3);
                        break;
                    case 10: // 투자 기간
                        builder.investmentPeriodRange(answer.getChoice() == ChoiceType.A ? PeriodRange.SHORT_TERM : PeriodRange.LONG_TERM);
                        break;
                    case 11: // 손실 감수 한도
                        builder.lossToleranceRange(answer.getChoice() == ChoiceType.A ? RiskRange.LOW_RISK : RiskRange.HIGH_RISK);
                        break;
                }
            }
        }
        
        // PREFERENCE 응답 처리
        if (preferenceAnswers != null) {
            for (AnswerDTO answer : preferenceAnswers) {
                switch (answer.getQuestionId().intValue()) {
                    case 12: // 투자 방식
                        builder.investmentStyle(answer.getChoice() == ChoiceType.A ? InvestmentStyle.LUMP_SUM : InvestmentStyle.REGULAR);
                        break;
                    case 13: // 우선순위
                        builder.priority(answer.getChoice() == ChoiceType.A ? Priority.SAFETY : Priority.RETURN);
                        break;
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * 사용자 데이터 저장
     */
    private void saveUserData(Long userId, Long investmentTypeId, UserInvestmentProfile userProfile, List<ConstraintType> constraints) {
        // 1. 기존 투자유형 저장
        saveUserInvestmentType(userId, investmentTypeId);
        
        // 2. 사용자 프로필 저장
        userProfile.setUserId(userId);
        UserInvestmentProfile existingProfile = userInvestmentProfileMapper.findByUserId(userId);
        if (existingProfile == null) {
            userInvestmentProfileMapper.insertUserInvestmentProfile(userProfile);
        } else {
            userProfile.setId(existingProfile.getId());
            userInvestmentProfileMapper.updateUserInvestmentProfile(userProfile);
        }
        
        // 3. 제약조건 저장
        userInvestmentConstraintsMapper.deactivateAllConstraints(userId); // 기존 제약조건 비활성화
        for (ConstraintType constraintType : constraints) {
            UserInvestmentConstraints constraint = UserInvestmentConstraints.builder()
                .userId(userId)
                .constraintType(constraintType)
                .isActive(true)
                .build();
            userInvestmentConstraintsMapper.insertUserInvestmentConstraints(constraint);
        }
    }
    
    /**
     * 제약조건과 프로필을 고려한 필터링된 추천상품 조회
     */
    private List<RecommendedProductDTO> getFilteredRecommendedProducts(Long investmentTypeId, List<ConstraintType> constraints, UserInvestmentProfile userProfile) {
        try {
            // 새로운 필터링 서비스 사용
            return enhancedProductRecommendationService.getFilteredRecommendedProducts(investmentTypeId, constraints, userProfile);
        } catch (Exception e) {
            log.error("새로운 추천 서비스 오류, 기존 서비스로 fallback: {}", e.getMessage());
            // 기존 추천 로직으로 fallback
            return productRecommendationService.getRecommendedProducts(investmentTypeId);
        }
    }
    
    /**
     * 사용자 제약조건 조회
     */
    private List<ConstraintType> getUserConstraints(Long userId) {
        return userInvestmentConstraintsMapper.findActiveConstraintsByUserId(userId)
                .stream()
                .map(UserInvestmentConstraints::getConstraintType)
                .collect(Collectors.toList());
    }

    
    private List<AnswerDTO> parseAnswers(Map<String, Object> payload) {
        Object rawAnswers = payload.get("answers");
        if (!(rawAnswers instanceof List<?> answersRaw)) {
            throw new IllegalArgumentException("answers 필드가 올바르지 않습니다.");
        }
        return answersRaw.stream()
                .map(this::parseAnswer)
                .toList();
    }

    private AnswerDTO parseAnswer(Object answerObj) {
        if (!(answerObj instanceof Map<?, ?> answerMap)) {
            throw new IllegalArgumentException("answers 배열의 요소가 올바르지 않습니다.");
        }

        Object questionId = answerMap.get("question_id");
        Object choice = answerMap.get("choice");

        if (questionId == null || choice == null) {
            throw new IllegalArgumentException("question_id 또는 choice 값이 누락되었습니다.");
        }

        try {
            return AnswerDTO.builder()
                    .questionId(Long.valueOf(questionId.toString()))
                    .choice(ChoiceType.valueOf(choice.toString()))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("답변 데이터 변환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 제약조건을 고려한 투자유형 계산
     */
    private Long calculateBestInvestmentTypeWithConstraints(List<AnswerDTO> personalityAnswers, List<ConstraintType> constraints) {
        log.info("제약조건을 고려한 투자유형 계산 시작 - 제약조건: {}", constraints);
        
        // 1. 기본 성향 점수 계산
        Map<Long, Integer> typeScores = new HashMap<>();
        if (personalityAnswers != null) {
            for (AnswerDTO answer : personalityAnswers) {
                addScoreForAnswer(answer, typeScores);
            }
        }
        
        log.info("기본 성향 점수: {}", typeScores);
        
        // 2. 제약조건에 따른 투자유형 조정
        if (constraints != null && !constraints.isEmpty()) {
            
            // HIGH_RISK_FORBIDDEN: 고위험 상품 금지 → 공격형 배제
            if (constraints.contains(ConstraintType.HIGH_RISK_FORBIDDEN)) {
                log.info("고위험 상품 금지 제약조건 적용 - 공격형 제거");
                typeScores.remove(3L); // 공격형 완전 제거
                typeScores.put(1L, typeScores.getOrDefault(1L, 0) + 100); // 안정형 강력 보너스
                typeScores.put(2L, typeScores.getOrDefault(2L, 0) + 50);  // 중립형 보너스
            }
            
            // PRINCIPAL_GUARANTEE: 원금보장 필수 → 공격형 배제, 안정형 우선
            if (constraints.contains(ConstraintType.PRINCIPAL_GUARANTEE)) {
                log.info("원금보장 필수 제약조건 적용 - 공격형 제거, 안정형 우선");
                typeScores.remove(3L); // 공격형 완전 제거
                typeScores.put(1L, typeScores.getOrDefault(1L, 0) + 150); // 안정형 최우선
                typeScores.put(2L, typeScores.getOrDefault(2L, 0) + 30);  // 중립형 약간 보너스
            }
            
            // LIQUIDITY_REQUIRED: 유동성 필수 → 복잡한 상품 배제, 안정형/중립형 우선
            if (constraints.contains(ConstraintType.LIQUIDITY_REQUIRED)) {
                log.info("유동성 필수 제약조건 적용 - 안정형/중립형 우선");
                typeScores.put(3L, Math.max(0, typeScores.getOrDefault(3L, 0) - 50)); // 공격형 감점
                typeScores.put(1L, typeScores.getOrDefault(1L, 0) + 80); // 안정형 보너스
                typeScores.put(2L, typeScores.getOrDefault(2L, 0) + 70); // 중립형 보너스
            }
        }
        
        log.info("제약조건 적용 후 점수: {}", typeScores);
        
        Long finalTypeId = findTypeWithHighestScore(typeScores);
        log.info("최종 결정된 투자유형: {} ({})", finalTypeId, getInvestmentTypeName(finalTypeId));
        
        return finalTypeId;
    }
    
    /**
     * 투자유형 이름 조회 (로깅용)
     */
    private String getInvestmentTypeName(Long typeId) {
        switch (typeId.intValue()) {
            case 1: return "안정형";
            case 2: return "중립형"; 
            case 3: return "공격형";
            default: return "알 수 없음";
        }
    }

    private void addScoreForAnswer(AnswerDTO answer, Map<Long, Integer> typeScores) {
        QuestionChoiceScore score = questionChoiceScoreMapper
                .findByQuestionIdAndChoice(answer.getQuestionId(), answer.getChoice());

        if (score != null) {
            typeScores.merge(score.getInvestmentTypeId(), score.getScore(), Integer::sum);
        }
    }

    private Long findTypeWithHighestScore(Map<Long, Integer> typeScores) {
        return typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1L); // 기본값: 안전형
    }

    private InvestmentType getInvestmentType(Long typeId) {
        return Optional.ofNullable(investmentTypeMapper.findById(typeId))
                .orElse(createDefaultInvestmentType());
    }

    private InvestmentType createDefaultInvestmentType() {
        InvestmentType defaultType = new InvestmentType();
        defaultType.setInvestmentTypeName("알 수 없음");
        defaultType.setInvestmentTypeDesc("투자성향을 확인할 수 없습니다.");
        return defaultType;
    }

    private void saveUserInvestmentType(Long userId, Long investmentTypeId) {
        UserInvestmentType userType = Optional.ofNullable(userInvestmentTypeMapper.findByUserId(userId))
                .orElse(new UserInvestmentType());

        userType.setUserId(userId);
        userType.setInvestmentTypeId(investmentTypeId);
        userType.setEvaluationDate(LocalDate.now());

        if (userType.getId() == null) {
            userInvestmentTypeMapper.insertUserInvestmentType(userType);
        } else {
            userInvestmentTypeMapper.updateUserInvestmentType(userType);
        }
    }

    private TypeTestResultDTO createSuccessResult(Long userId, InvestmentType investmentType, List<RecommendedProductDTO> recommendedProducts) {
        return TypeTestResultDTO.builder()
                .userId(userId)
                .investmentTypeId(investmentType.getId())
                .investmentTypeName(investmentType.getInvestmentTypeName())
                .investmentTypeDesc(investmentType.getInvestmentTypeDesc())
                .recommendedProducts(recommendedProducts)
                .message("투자유형 계산이 완료되었습니다.")
                .build();
    }

    private TypeTestResultDTO createFailResult(String message) {
        return TypeTestResultDTO.builder()
                .message(message)
                .build();
    }
}

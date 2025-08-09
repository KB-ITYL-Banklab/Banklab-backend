package com.banklab.financeContents.service;

import com.banklab.financeContents.domain.FinanceQuizVO;
import com.banklab.financeContents.domain.UserQuizResultVO;
import com.banklab.financeContents.dto.*;
import com.banklab.financeContents.mapper.FinanceQuizMapper;
import com.banklab.financeContents.mapper.UserQuizResultMapper;
import com.banklab.security.util.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// 경제 퀴즈 서비스 구현체
@Service
@RequiredArgsConstructor
public class FinanceQuizServiceImpl implements FinanceQuizService {

    @Autowired
    private FinanceQuizMapper financeQuizMapper;

    private final LoginUserProvider loginUserProvider;
    
    @Autowired
    private UserQuizResultMapper userQuizResultMapper;

    // 기준 날짜 (상수) - 2025년 7월 1일
    private static final LocalDate BASE_DATE = LocalDate.of(2025, 7, 28);
    
    // 총 문제 수 (201개)
    private static final int TOTAL_QUIZ_COUNT = 201;
    
    // 일일 퀴즈 문제 수 (3문제)
    private static final int DAILY_QUIZ_COUNT = 3;

    @Override
    public List<FinanceQuizDTO> getAllQuizzes() {
        List<FinanceQuizVO> quizzes = financeQuizMapper.findAllQuizzes();
        return quizzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinanceQuizDTO> getQuizzesByType(String quizType) {
        List<FinanceQuizVO> quizzes = financeQuizMapper.findQuizzesByType(quizType);
        return quizzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FinanceQuizDTO getQuizById(Integer id) {
        FinanceQuizVO quiz = financeQuizMapper.findQuizById(id);
        return quiz != null ? convertToDTO(quiz) : null;
    }

    @Override
    public List<FinanceQuizDTO> getRandomQuizzes(int count) {
        // /random 엔드포인트에서는 오늘의 퀴즈를 반환
        return getTodayQuizzes();
    }

    @Override
    public List<FinanceQuizDTO> getRandomQuizzesByType(String quizType, int count) {
        List<FinanceQuizVO> quizzes = financeQuizMapper.findRandomQuizzesByType(quizType, count);
        return quizzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuizResultDTO checkAnswer(QuizAnswerRequestDTO answerRequest) {
        FinanceQuizVO quiz = financeQuizMapper.findQuizById(answerRequest.getQuizId());
        
        if (quiz == null) {
            throw new IllegalArgumentException("존재하지 않는 퀴즈입니다.");
        }

        String correctAnswer = quiz.getAnswer();
        String userAnswer = answerRequest.getUserAnswer();
        boolean isCorrect = correctAnswer.equals(userAnswer);

        return new QuizResultDTO(isCorrect, correctAnswer, quiz.getExplanation(), userAnswer);
    }

    @Override
    public int getTotalQuizCount() {
        return financeQuizMapper.getTotalQuizCount();
    }

    @Override
    public int getQuizCountByType(String quizType) {
        return financeQuizMapper.getQuizCountByType(quizType);
    }

    @Override
    public List<String> getAvailableQuizTypes() {
        return Arrays.asList("객관식퀴즈", "OX퀴즈", "단답형퀴즈");
    }

    @Override
    public List<FinanceQuizDTO> getTodayQuizzes() {
        // 1. 날짜 차이 계산
        LocalDate today = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(BASE_DATE, today);
        
        // 2. 시작 인덱스 계산: (차이 % 67 * 3) + 1
        int startIndex = calculateStartIndex(daysDifference);
        
        // 3. 연속 3문제 조회
        return getConsecutiveQuizzes(startIndex, DAILY_QUIZ_COUNT);
    }

    @Override
    public DailyQuizResultDTO processDailyQuizResults(DailyQuizRequestDTO request) {
        System.out.println("=== processDailyQuizResults 시작 ===");
        System.out.println("Request: " + request);
        Long memberId = loginUserProvider.getLoginMemberId();
        
        // 입력 검증
        if (request == null) {
            throw new IllegalArgumentException("요청 데이터가 null입니다.");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("memberId가 null입니다.");
        }
        if (request.getUserAnswer() == null) {
            throw new IllegalArgumentException("userAnswer가 null입니다.");
        }
        
        // 오늘 이미 퀴즈를 풀었는지 확인
        if (hasUserSolvedTodayQuiz(memberId)) {
            throw new IllegalStateException("오늘은 이미 퀴즈를 완료하셨습니다. 내일 다시 도전해주세요!");
        }
        
        // 1. 오늘의 퀴즈 정답 가져오기
        List<FinanceQuizDTO> todayQuizzes = getTodayQuizzes();
        if (todayQuizzes.size() != 3) {
            throw new IllegalStateException("오늘의 퀴즈는 정확히 3문제여야 합니다.");
        }
        
        // 2. 사용자 답안 파싱 (예: "3O1" -> ['3', 'O', '1'])
        String userAnswers = request.getUserAnswer();
        if (userAnswers.length() != 3) {
            throw new IllegalArgumentException("답안은 정확히 3개 문자여야 합니다. (예: '3O1'). 현재: '" + userAnswers + "' (길이: " + userAnswers.length() + ")");
        }
        
        // 3. 각 문제별 채점 및 포인트 계산
        int correctCount = 0;
        int earnedPoints = 0;
        List<QuizResultDTO> detailResults = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            // 사용자 답안 (한 글자씩)
            String userAnswer = String.valueOf(userAnswers.charAt(i));
            
            // 정답
            String correctAnswer = todayQuizzes.get(i).getAnswer();
            
            // 정답 여부 확인
            boolean isCorrect = isAnswerCorrect(userAnswer, correctAnswer, todayQuizzes.get(i).getQuizType());
            
            // 포인트 계산
            if (isCorrect) {
                earnedPoints += 5; // 정답: 5P
                correctCount++;
            } else {
                earnedPoints += 3; // 오답: 3P
            }
            
            // 상세 결과 저장
            detailResults.add(new QuizResultDTO(
                isCorrect, 
                correctAnswer, 
                todayQuizzes.get(i).getExplanation(), 
                userAnswer
            ));
        }
        
        // 4. 기존 데이터 조회 (사용자별 단일 레코드)
        UserQuizResultVO existingResult = userQuizResultMapper.getUserQuizResult(memberId);
        
        int currentProblemCount = 0;
        int currentCorrectProblemCount = 0; 
        int previousPoints = 0;
        
        if (existingResult != null) {
            currentProblemCount = existingResult.getProblem() != null ? existingResult.getProblem() : 0;
            currentCorrectProblemCount = existingResult.getCorrectProblem() != null ? existingResult.getCorrectProblem() : 0;
            previousPoints = existingResult.getAccumulatedPoints() != null ? existingResult.getAccumulatedPoints() : 0;
        }
        
        // 5. 새로운 값 계산
        int newProblemCount = currentProblemCount + 3;
        int newCorrectProblemCount = currentCorrectProblemCount + correctCount;
        int totalAccumulatedPoints = previousPoints + earnedPoints;
        
        // 6. DB에 결과 Upsert (Insert 또는 Update)
        UserQuizResultVO userQuizResult = new UserQuizResultVO();
        userQuizResult.setMemberId(memberId);
        userQuizResult.setUserAnswer(userAnswers); // "3O1" 형태로 저장 (최신 답안)
        userQuizResult.setProblem(newProblemCount);
        userQuizResult.setCorrectProblem(newCorrectProblemCount);
        userQuizResult.setAccumulatedPoints(totalAccumulatedPoints);
        
        System.out.println("=== DB Upsert 시도 ===");
        System.out.println("저장할 데이터 - memberId: " + userQuizResult.getMemberId());
        System.out.println("저장할 데이터 - userAnswer: " + userQuizResult.getUserAnswer());
        System.out.println("저장할 데이터 - problem: " + userQuizResult.getProblem());
        System.out.println("저장할 데이터 - correctProblem: " + userQuizResult.getCorrectProblem());
        System.out.println("저장할 데이터 - accumulatedPoints: " + userQuizResult.getAccumulatedPoints());
        
        int upsertResult = userQuizResultMapper.upsertUserQuizResult(userQuizResult);
        System.out.println("DB Upsert 결과 (영향받은 행 수): " + upsertResult);
        
        if (upsertResult <= 0) {
            throw new RuntimeException("DB Upsert에 실패했습니다.");
        }
        
        return new DailyQuizResultDTO(
            correctCount, 
            DAILY_QUIZ_COUNT, 
            earnedPoints, 
            totalAccumulatedPoints, 
            detailResults
        );
    }

    /**
     * 답안 정답 여부 확인
     * @param userAnswer 사용자 답안 (예: "3", "1", "2")
     * @param correctAnswer 정답 (예: "3", "1", "2") 
     * @param quizType 퀴즈 타입
     * @return 정답 여부
     */
    private boolean isAnswerCorrect(String userAnswer, String correctAnswer, String quizType) {
        // 답안 정규화 후 비교
        String normalizedUserAnswer = normalizeAnswer(userAnswer, quizType);
        String normalizedCorrectAnswer = normalizeAnswer(correctAnswer, quizType);
        
        return normalizedCorrectAnswer.equals(normalizedUserAnswer);
    }
    
    /**
     * 답안 정규화 (다양한 형태의 답안을 통일)
     * @param answer 원본 답안
     * @param quizType 퀴즈 타입
     * @return 정규화된 답안
     */
    private String normalizeAnswer(String answer, String quizType) {
        if (answer == null) return "";
        
        // 공백 제거 및 대소문자 통일
        answer = answer.trim().toUpperCase();
        
        // OX 퀴즈인 경우: O→1, X→2 변환
        if ("OX퀴즈".equals(quizType)) {
            answer = answer.replace("O", "1")
                          .replace("X", "2");
        }
        
        // 객관식 퀴즈인 경우: 특수 문자 변환
        if ("객관식퀴즈".equals(quizType)) {
            answer = answer.replace("①", "1")
                          .replace("②", "2") 
                          .replace("③", "3")
                          .replace("④", "4")
                          .replace("⑤", "5");
        }
        
        return answer;
    }

    @Override
    public boolean hasUserSolvedTodayQuiz(Long memberId) {
        if (memberId == null) {
            return false;
        }
        int todayCount = userQuizResultMapper.getTodayQuizCount(memberId);
        return todayCount > 0;
    }

    @Override
    public DailyQuizResultDTO getTodayQuizResult(Long memberId) {
        if (memberId == null) {
            return null;
        }
        
        // 1. DB에서 사용자 퀴즈 결과 조회 (오늘 업데이트 여부 확인)
        UserQuizResultVO todayResult = userQuizResultMapper.getTodayQuizResult(memberId);
        if (todayResult == null) {
            return null;
        }
        
        // 2. 오늘의 퀴즈 문제들 가져오기
        List<FinanceQuizDTO> todayQuizzes = getTodayQuizzes();
        if (todayQuizzes.size() != 3) {
            throw new IllegalStateException("오늘의 퀴즈는 정확히 3문제여야 합니다.");
        }
        
        // 3. 사용자 답안 파싱 (예: "3O1" -> ['3', 'O', '1'])
        String userAnswers = todayResult.getUserAnswer();
        if (userAnswers == null || userAnswers.length() != 3) {
            // 최신 답안이 없으면 빈 결과 반환
            return new DailyQuizResultDTO(
                0, 3, 0, 
                todayResult.getAccumulatedPoints(), 
                new ArrayList<>()
            );
        }
        
        // 4. 각 문제별 결과 재생성
        int correctCount = 0;
        List<QuizResultDTO> detailResults = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            // 사용자 답안 (한 글자씩)
            String userAnswer = String.valueOf(userAnswers.charAt(i));
            
            // 정답
            String correctAnswer = todayQuizzes.get(i).getAnswer();
            
            // 정답 여부 확인
            boolean isCorrect = isAnswerCorrect(userAnswer, correctAnswer, todayQuizzes.get(i).getQuizType());
            
            if (isCorrect) {
                correctCount++;
            }
            
            // 상세 결과 생성
            detailResults.add(new QuizResultDTO(
                isCorrect, 
                correctAnswer, 
                todayQuizzes.get(i).getExplanation(), 
                userAnswer
            ));
        }
        
        // 5. 결과 DTO 생성 및 반환
        return new DailyQuizResultDTO(
            correctCount, 
            3, // 총 문제 수
            0, // 획득 포인트 (계산하지 않음, 이미 저장된 값 사용)
            todayResult.getAccumulatedPoints(), // DB에서 가져온 누적 포인트
            detailResults
        );
    }

    @Override
    public UserQuizStatsDTO getUserQuizStats(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId는 null일 수 없습니다.");
        }
        
        UserQuizStatsDTO stats = userQuizResultMapper.getUserQuizStats(memberId);
        
        // 데이터가 없는 경우 기본값 반환
        if (stats == null) {
            stats = new UserQuizStatsDTO(memberId, 0, 0, 0.0, 0);
        }
        
        return stats;
    }

    /**
     * 시작 인덱스 계산
     * 공식: (차이 % 67 * 3) + 1
     */
    private int calculateStartIndex(long daysDifference) {
        long index = (daysDifference % 67 * 3) + 1;
        
        // 201개 문제 범위 내에서 3개 연속 선택 가능하도록 조정
        int maxStartIndex = TOTAL_QUIZ_COUNT - DAILY_QUIZ_COUNT + 1; // 199
        if (index > maxStartIndex) {
            index = (index % maxStartIndex) + 1;
        }
        
        return (int) index;
    }

    /**
    /**
     * 연속 3문제 조회
     */
    private List<FinanceQuizDTO> getConsecutiveQuizzes(int startIndex, int count) {
        int endIndex = startIndex + count - 1;
        List<FinanceQuizVO> quizzes = financeQuizMapper.findQuizzesByIndexRange(startIndex, endIndex);
        return quizzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * VO를 DTO로 변환
     * @param vo 퀴즈 VO
     * @return 퀴즈 DTO
     */
    private FinanceQuizDTO convertToDTO(FinanceQuizVO vo) {
        return new FinanceQuizDTO(
                vo.getId(),
                vo.getQuizType(),
                vo.getQuestion(),
                vo.getOption1(),
                vo.getOption2(),
                vo.getOption3(),
                vo.getOption4(),
                vo.getAnswer(),
                vo.getExplanation()
        );
    }
}

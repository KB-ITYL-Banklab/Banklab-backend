package com.banklab.category.service;

// 테스트 대상인 CategoryService와 관련 도메인 객체들을 import
import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.kakaomap.service.KakaoMapService;
import com.banklab.category.mapper.CategoryMapper;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;

// JUnit5 테스트 프레임워크 관련 어노테이션들을 import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito 라이브러리로 모의 객체(Mock) 생성 및 검증을 위한 import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Java 표준 라이브러리 - 컬렉션, 예외 처리, 비동기 처리 등
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

// 테스트 검증을 위한 assertion과 mockito 검증 메서드들
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit5와 Mockito를 사용하여 CategoryService를 테스트하는 클래스
 * 카테고리 관련 비즈니스 로직과 비동기 처리를 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 테스트") // 테스트 실행 시 표시될 이름
class CategoryServiceTest {

    // @Mock: Mockito가 자동으로 모의 객체를 생성해주는 어노테이션들
    @Mock
    private CategoryMapper categoryMapper; // 카테고리 데이터베이스 접근을 위한 MyBatis 매퍼 모의 객체

    @Mock
    private TransactionMapper transactionMapper; // 거래내역 데이터베이스 접근을 위한 MyBatis 매퍼 모의 객체

    @Mock
    private KakaoMapService kakaoMapService; // 카카오맵 API 호출 및 Redis 캐시 관련 서비스 모의 객체

    @Mock
    private SummaryBatchService summaryBatchService; // 집계 배치 서비스 모의 객체

    // @InjectMocks: 위에서 생성한 Mock 객체들을 자동으로 주입받는 실제 테스트 대상 객체
    @InjectMocks
    private CategoryService categoryService; // 실제 테스트할 카테고리 서비스 객체

    // 테스트에서 공통으로 사용할 테스트 데이터 변수들
    private List<TransactionHistoryVO> testTransactions; // 테스트용 거래 내역 리스트
    private CategoryVO testCategory; // 테스트용 카테고리 도메인 객체
    private CategoryDTO testCategoryDTO; // 테스트용 카테고리 DTO 객체

    /**
     * 각 테스트 메서드 실행 전에 공통 테스트 데이터를 초기화하는 메서드
     * @BeforeEach로 모든 테스트 메서드 실행 전마다 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 거래 내역 리스트 생성 - 3개의 거래 내역 포함 (중복 상호명 포함)
        testTransactions = Arrays.asList(
                // 첫 번째 거래: 스타벅스에서 5000원 출금
                TransactionHistoryVO.builder()
                        .id(1L) // 거래 고유 ID
                        .memberId(1L) // 거래한 회원 ID
                        .accountId(1L) // 거래가 발생한 계좌 ID
                        .resAccount("123456789") // 거래 발생 계좌번호
                        .transactionDate("20240115") // 거래 발생일 (yyyyMMdd 형식)
                        .transactionTime("143000") // 거래 발생시간 (HHmmss 형식)
                        .resAccountOut(5000L) // 출금액
                        .resAfterTranBalance(95000L) // 거래 후 잔액
                        .description("스타벅스 강남점") // 거래 상대방(상호명) - 카테고리 분류의 기준
                        .build(),
                // 두 번째 거래: 올리브영에서 15000원 출금
                TransactionHistoryVO.builder()
                        .id(2L)
                        .memberId(1L)
                        .accountId(1L)
                        .resAccount("123456789")
                        .transactionDate("20240116")
                        .transactionTime("160000")
                        .resAccountOut(15000L)
                        .resAfterTranBalance(80000L)
                        .description("올리브영 홍대점") // 다른 상호명
                        .build(),
                // 세 번째 거래: 동일한 스타벅스에서 4500원 출금 (중복 상호명으로 중복 제거 테스트용)
                TransactionHistoryVO.builder()
                        .id(3L)
                        .memberId(1L)
                        .accountId(1L)
                        .resAccount("123456789")
                        .transactionDate("20240117")
                        .transactionTime("090000")
                        .resAccountOut(4500L)
                        .resAfterTranBalance(75500L)
                        .description("스타벅스 강남점")  // 첫 번째와 동일한 상호명 (중복)
                        .build()
        );

        // 테스트용 카테고리 도메인 객체 생성 - Builder 패턴 사용
        testCategory = CategoryVO.builder()
                .id(1L) // 카테고리 고유 ID
                .name("카페") // 카테고리명
                .build();

        // 테스트용 카테고리 DTO 객체 생성 - Builder 패턴 사용
        testCategoryDTO = CategoryDTO.builder()
                .id(1L) // DTO의 카테고리 고유 ID
                .name("카페") // DTO의 카테고리명
                .build();
    }

    /**
     * 거래 내역을 기반으로 카테고리를 비동기적으로 분류하는 기능의 정상 동작 테스트
     * 외부 API 호출과 데이터베이스 저장이 올바르게 수행되는지 검증
     */
    @Test
    @DisplayName("카테고리 분류 - 정상 케이스")
    void categorizeTransactions_Success() throws Exception {
        // given: Mock 객체의 동작 정의
        when(kakaoMapService.isStoredInRedis(anyString())).thenReturn(null); // Redis 캐시에 없음 (캐시 미스)
        when(kakaoMapService.getCategoryByDesc(anyString(), anyString())).thenReturn(1L); // API 호출로 카테고리 ID 1 반환

        // when: 실제 테스트할 메서드 호출 - 비동기 메서드이므로 CompletableFuture 반환
        CompletableFuture<Void> result = categoryService.categorizeTransactions(testTransactions, anyString());
        result.get(); // 비동기 작업 완료까지 대기 (블로킹)

        // then: 결과 검증
        verify(transactionMapper, times(1)).updateCategories(any()); // 데이터베이스 업데이트가 1번 호출됨
        // 중복 상호명 제거로 "스타벅스 강남점", "올리브영 홍대점" 2개만 API 호출됨을 검증
        verify(kakaoMapService, times(2)).getCategoryByDesc(anyString(), anyString());
    }

    /**
     * Redis 캐시에서 카테고리 정보를 찾을 수 있는 경우의 테스트
     * 외부 API 호출 없이 캐시된 데이터만 사용하는지 검증
     */
    @Test
    @DisplayName("카테고리 분류 - Redis 캐시 적중")
    void categorizeTransactions_CacheHit() throws Exception {
        // given: Redis 캐시에서 카테고리 ID를 찾을 수 있도록 설정
        when(kakaoMapService.isStoredInRedis(anyString())).thenReturn(1L); // 캐시 적중 - 카테고리 ID 1 반환

        // when: 비동기 카테고리 분류 메서드 호출
        CompletableFuture<Void> result = categoryService.categorizeTransactions(testTransactions,anyString());
        result.get(); // 비동기 작업 완료 대기

        // then: 결과 검증
        verify(transactionMapper, times(1)).updateCategories(any()); // 데이터베이스 업데이트 수행됨
        verify(kakaoMapService, never()).getCategoryByDesc(anyString(), anyString()); // 캐시 적중으로 API 호출하지 않음
    }

    /**
     * 빈 거래 내역 리스트가 전달되었을 때의 동작 테스트
     * 빈 리스트라도 데이터베이스 업데이트는 호출되지만 실제 API 호출은 없어야 함
     */
    @Test
    @DisplayName("카테고리 분류 - 빈 거래내역")
    void categorizeTransactions_EmptyTransactions() throws Exception {
        // given: 빈 거래 내역 리스트 생성
        List<TransactionHistoryVO> emptyTransactions = Collections.emptyList();

        // when: 빈 리스트로 카테고리 분류 메서드 호출
        CompletableFuture<Void> result = categoryService.categorizeTransactions(emptyTransactions, anyString());
        result.get(); // 비동기 작업 완료 대기

        // then: 결과 검증
        verify(transactionMapper, times(1)).updateCategories(emptyTransactions); // 빈 리스트로도 업데이트 호출됨
        verify(kakaoMapService, never()).getCategoryByDesc(anyString(), anyString()); // 처리할 데이터가 없어 API 호출 안됨
    }

    /**
     * Redis 캐시 미스 상황에서 외부 API 호출을 통한 카테고리 조회 테스트
     */
    @Test
    @DisplayName("캐시와 함께 카테고리 조회 - 캐시 미스")
    void getCategoryWithCache_CacheMiss() {
        // given: 테스트 데이터 설정
        String keyword = "스타벅스"; // 검색할 키워드 (상호명)
        String redisKey = "category::" + keyword; // Redis에 저장될 키 형식
        when(kakaoMapService.isStoredInRedis(redisKey)).thenReturn(null); // 캐시에 없음
        when(kakaoMapService.getCategoryByDesc(redisKey, keyword)).thenReturn(1L); // API 호출로 카테고리 ID 1 반환

        // when: 캐시와 함께 카테고리 조회 메서드 호출
        Long result = categoryService.getCategoryWithCache(keyword);

        // then: 결과 검증
        assertEquals(1L, result); // 반환된 카테고리 ID가 1인지 확인
        verify(kakaoMapService, times(1)).isStoredInRedis(redisKey); // 캐시 확인 1번 호출
        verify(kakaoMapService, times(1)).getCategoryByDesc(redisKey, keyword); // API 호출 1번 수행
    }

    /**
     * Redis 캐시 적중 상황에서 캐시된 데이터만 사용하는 테스트
     */
    @Test
    @DisplayName("캐시와 함께 카테고리 조회 - 캐시 적중")
    void getCategoryWithCache_CacheHit() {
        // given: 캐시에 데이터가 있는 상황 설정
        String keyword = "스타벅스";
        String redisKey = "category::" + keyword;
        when(kakaoMapService.isStoredInRedis(redisKey)).thenReturn(1L); // 캐시에서 카테고리 ID 1 반환

        // when: 캐시와 함께 카테고리 조회 메서드 호출
        Long result = categoryService.getCategoryWithCache(keyword);

        // then: 결과 검증
        assertEquals(1L, result); // 캐시에서 가져온 카테고리 ID가 1인지 확인
        verify(kakaoMapService, times(1)).isStoredInRedis(redisKey); // 캐시 확인 1번 호출
        verify(kakaoMapService, never()).getCategoryByDesc(anyString(), anyString()); // 캐시 적중으로 API 호출 안됨
    }

    /**
     * 모든 카테고리 목록을 조회하는 기능 테스트
     * 데이터베이스에서 조회한 VO 객체들이 DTO로 올바르게 변환되는지 검증
     */
    @Test
    @DisplayName("모든 카테고리 조회")
    void findAll_Success() {
        // given: 데이터베이스에서 반환될 카테고리 VO 리스트 설정
        List<CategoryVO> categoryVOs = Arrays.asList(testCategory); // 테스트 카테고리 1개 포함
        when(categoryMapper.findAll()).thenReturn(categoryVOs); // mapper 호출 시 위 리스트 반환

        // when: 모든 카테고리 조회 메서드 호출
        List<CategoryDTO> result = categoryService.findAll();

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        assertEquals(1, result.size()); // 리스트 크기가 1개
        assertEquals(testCategory.getName(), result.get(0).getName()); // VO에서 DTO로 변환 시 이름이 동일
        verify(categoryMapper, times(1)).findAll(); // mapper의 findAll이 1번 호출됨
    }

    /**
     * 카테고리 ID로 특정 카테고리를 조회하는 기능의 정상 동작 테스트
     */
    @Test
    @DisplayName("ID로 카테고리 조회 - 정상 케이스")
    void getCategoryById_Success() {
        // given: 조회할 카테고리 ID와 예상 반환값 설정
        Long categoryId = 1L;
        when(categoryMapper.getCategoryById(categoryId)).thenReturn(testCategory); // ID로 조회 시 테스트 카테고리 반환

        // when: ID로 카테고리 조회 메서드 호출
        CategoryDTO result = categoryService.getCategoryById(categoryId);

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        assertEquals(testCategory.getId(), result.getId()); // ID가 동일
        assertEquals(testCategory.getName(), result.getName()); // 이름이 동일
        verify(categoryMapper, times(1)).getCategoryById(categoryId); // mapper 호출 1번 확인
    }

    /**
     * 존재하지 않는 카테고리 ID로 조회했을 때 예외 발생 테스트
     */
    @Test
    @DisplayName("ID로 카테고리 조회 - 존재하지 않는 경우")
    void getCategoryById_NotFound() {
        // given: 존재하지 않는 카테고리 ID 설정
        Long categoryId = 999L; // 존재하지 않는 ID
        when(categoryMapper.getCategoryById(categoryId)).thenReturn(null); // null 반환 (데이터 없음)

        // when & then: 예외 발생 확인
        assertThrows(NoSuchElementException.class, () -> {
            categoryService.getCategoryById(categoryId); // NoSuchElementException 예외가 발생해야 함
        });
        verify(categoryMapper, times(1)).getCategoryById(categoryId); // mapper 호출은 1번 수행됨
    }

    /**
     * 카테고리 이름으로 특정 카테고리를 조회하는 기능의 정상 동작 테스트
     */
    @Test
    @DisplayName("이름으로 카테고리 조회 - 정상 케이스")
    void getCategoryByName_Success() {
        // given: 조회할 카테고리 이름과 예상 반환값 설정
        String categoryName = "카페";
        when(categoryMapper.getCategoryByName(categoryName)).thenReturn(testCategory); // 이름으로 조회 시 테스트 카테고리 반환

        // when: 이름으로 카테고리 조회 메서드 호출
        CategoryDTO result = categoryService.getCategoryByName(categoryName);

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        assertEquals(testCategory.getName(), result.getName()); // 조회된 이름이 동일
        verify(categoryMapper, times(1)).getCategoryByName(categoryName); // mapper 호출 1번 확인
    }

    /**
     * 존재하지 않는 카테고리 이름으로 조회했을 때 예외 발생 테스트
     */
    @Test
    @DisplayName("이름으로 카테고리 조회 - 존재하지 않는 경우")
    void getCategoryByName_NotFound() {
        // given: 존재하지 않는 카테고리 이름 설정
        String categoryName = "존재하지않는카테고리";
        when(categoryMapper.getCategoryByName(categoryName)).thenReturn(null); // null 반환 (데이터 없음)

        // when & then: 예외 발생 확인
        assertThrows(NoSuchElementException.class, () -> {
            categoryService.getCategoryByName(categoryName); // NoSuchElementException 예외가 발생해야 함
        });
        verify(categoryMapper, times(1)).getCategoryByName(categoryName); // mapper 호출은 1번 수행됨
    }

    /**
     * 새로운 카테고리 생성 기능 테스트
     * 기존에 없는 카테고리명으로 새 카테고리를 생성하는 경우
     */
    @Test
    @DisplayName("카테고리 생성 - 새로운 카테고리")
    void createCategory_NewCategory() {
        // given: 새로운 카테고리 데이터 설정
        String categoryName = "새로운카테고리";
        CategoryVO newCategory = CategoryVO.builder()
                .id(2L) // 새로 생성될 카테고리의 ID
                .name(categoryName) // 새로운 카테고리명
                .build();

        // createCategory 메서드에서 getCategoryByName을 먼저 호출하므로 첫 번째 호출은 예외
        when(categoryMapper.getCategoryByName(categoryName))
                .thenThrow(new NoSuchElementException()); // 기존에 없어서 예외 발생
        when(categoryMapper.getCategoryById(2L)).thenReturn(newCategory); // 생성 후 ID로 조회 시 새 카테고리 반환
        
        // createCategory 호출 시 id가 설정됨을 모킹 - doAnswer로 매개변수 조작
        doAnswer(invocation -> {
            CategoryVO vo = invocation.getArgument(0); // 첫 번째 매개변수(CategoryVO) 가져오기
            vo.setId(2L); // ID를 2L로 설정 (실제 데이터베이스에서 자동 생성되는 상황을 모킹)
            return null; // void 메서드이므로 null 반환
        }).when(categoryMapper).createCategory(any(CategoryVO.class));

        // when: 새 카테고리 생성 메서드 호출
        CategoryDTO result = categoryService.createCategory(categoryName);

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        assertEquals(categoryName, result.getName()); // 생성된 카테고리명이 동일
        verify(categoryMapper, times(1)).createCategory(any(CategoryVO.class)); // 실제 생성 메서드 1번 호출
        verify(categoryMapper, times(1)).getCategoryById(2L); // 생성 후 ID로 조회 1번 호출
    }

    /**
     * 이미 존재하는 카테고리명으로 생성을 시도했을 때의 동작 테스트
     * 새로 생성하지 않고 기존 카테고리 정보를 반환해야 함
     */
    @Test
    @DisplayName("카테고리 생성 - 이미 존재하는 카테고리")
    void createCategory_ExistingCategory() {
        // given: 이미 존재하는 카테고리명 설정
        String categoryName = "카페";
        // createCategory 메서드는 getCategoryByName을 try-catch로 감싸서 호출하므로
        // 존재하는 카테고리의 경우 정상적으로 CategoryDTO를 반환함
        when(categoryMapper.getCategoryByName(categoryName)).thenReturn(testCategory); // 기존 카테고리 반환

        // when: 기존 카테고리명으로 생성 시도
        CategoryDTO result = categoryService.createCategory(categoryName);

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        assertEquals(categoryName, result.getName()); // 기존 카테고리명과 동일
        verify(categoryMapper, never()).createCategory(any(CategoryVO.class)); // 실제 생성 메서드는 호출되지 않음
        verify(categoryMapper, times(1)).getCategoryByName(categoryName); // 기존 카테고리 조회만 1번 호출
    }

    /**
     * 카테고리 정보 업데이트 기능의 정상 동작 테스트
     */
    @Test
    @DisplayName("카테고리 업데이트 - 정상 케이스")
    void updateCategory_Success() {
        // given: 업데이트할 카테고리 정보 설정
        CategoryDTO updateDTO = CategoryDTO.builder()
                .id(1L) // 업데이트할 카테고리 ID
                .name("업데이트된카테고리") // 새로운 카테고리명
                .build();

        // 업데이트 대상 카테고리가 존재한다고 설정
        when(categoryMapper.getCategoryByName(updateDTO.getName())).thenReturn(testCategory);
        when(categoryMapper.getCategoryById(testCategory.getId())).thenReturn(testCategory); // 업데이트 후 조회

        // when: 카테고리 업데이트 메서드 호출
        CategoryDTO result = categoryService.updateCategory(updateDTO);

        // then: 결과 검증
        assertNotNull(result); // 결과가 null이 아님
        verify(categoryMapper, times(1)).updateCategory(any(CategoryVO.class)); // 실제 업데이트 메서드 1번 호출
        verify(categoryMapper, times(1)).getCategoryById(testCategory.getId()); // 업데이트 후 조회 1번 호출
    }

    /**
     * 존재하지 않는 카테고리를 업데이트하려고 할 때의 동작 테스트
     * null을 반환하여 업데이트 실패를 알려야 함
     */
    @Test
    @DisplayName("카테고리 업데이트 - 존재하지 않는 카테고리")
    void updateCategory_NotFound() {
        // given: 존재하지 않는 카테고리 업데이트 시도
        CategoryDTO updateDTO = CategoryDTO.builder()
                .id(999L) // 존재하지 않는 ID
                .name("존재하지않는카테고리") // 존재하지 않는 이름
                .build();

        when(categoryMapper.getCategoryByName(updateDTO.getName())).thenReturn(null); // 해당 이름의 카테고리 없음

        // when: 존재하지 않는 카테고리 업데이트 시도
        CategoryDTO result = categoryService.updateCategory(updateDTO);

        // then: 결과 검증
        assertNull(result); // 업데이트 실패로 null 반환
        verify(categoryMapper, never()).updateCategory(any(CategoryVO.class)); // 실제 업데이트는 호출되지 않음
        verify(categoryMapper, times(1)).getCategoryByName(updateDTO.getName()); // 존재 여부 확인만 1번 호출
    }
}
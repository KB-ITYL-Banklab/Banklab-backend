package com.banklab.category.service;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.gemini.service.GeminiService;
import com.banklab.category.mapper.CategoryMapper;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CategoryService에 대한 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private GeminiService geminiService;
    @Mock
    private UtilService utilService;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("categorizeTransactions 메소드 테스트")
    class CategorizeTransactionsTest {

        @Test
        @DisplayName("모든 상호명이 내부 키워드로 분류될 경우, Gemini API를 호출하지 않아야 한다")
        void categorizeTransactions_WhenAllMappedInternally_ShouldNotCallGemini() {
            // given
            TransactionHistoryVO tx1 = new TransactionHistoryVO();
            tx1.setDescription("스타벅스");
            TransactionHistoryVO tx2 = new TransactionHistoryVO();
            tx2.setDescription("CGV");
            List<TransactionHistoryVO> transactions = Arrays.asList(tx1, tx2);

            // utilService가 Redis 캐시 miss(null) 후 내부 분류 성공(1L, 6L)을 반환하도록 설정
            when(utilService.isStoredInRedis(anyString())).thenReturn(null);
            when(utilService.mapToInternalCategory("스타벅스")).thenReturn(1L); // 카페
            when(utilService.mapToInternalCategory("CGV")).thenReturn(6L);     // 문화/여가

            // when
            categoryService.categorizeTransactions(transactions, "some-key");

            // then
            // Gemini 서비스는 호출되지 않아야 함
            verify(geminiService, never()).classifyCategories(anySet());
            // transactionMapper의 updateCategories가 호출되었는지 확인
            verify(transactionMapper).updateCategories(anyList());

            // 캡처를 통해 실제 업데이트된 카테고리 ID 검증
            ArgumentCaptor<List<TransactionHistoryVO>> captor = ArgumentCaptor.forClass(List.class);
            verify(transactionMapper).updateCategories(captor.capture());
            List<TransactionHistoryVO> updatedTransactions = captor.getValue();

            assertThat(updatedTransactions.get(0).getCategory_id()).isEqualTo(1L);
            assertThat(updatedTransactions.get(1).getCategory_id()).isEqualTo(6L);
        }

        @Test
        @DisplayName("내부 분류가 불가능한 상호명이 있을 경우, Gemini API를 호출하여 분류해야 한다")
        void categorizeTransactions_WhenInternalMappingFails_ShouldCallGemini() {
            // given
            TransactionHistoryVO tx1 = new TransactionHistoryVO();
            tx1.setDescription("스타벅스"); // 내부 분류 가능
            TransactionHistoryVO tx2 = new TransactionHistoryVO();
            tx2.setDescription("감성커피"); // Gemini로 분류해야 함
            List<TransactionHistoryVO> transactions = Arrays.asList(tx1, tx2);

            when(utilService.isStoredInRedis(anyString())).thenReturn(null);
            when(utilService.mapToInternalCategory("스타벅스")).thenReturn(1L);
            when(utilService.mapToInternalCategory("감성커피")).thenReturn(8L); // 내부 분류 실패(기타)

            // Gemini API가 "카페/간식"을 반환하도록 설정
            when(geminiService.classifyCategories(Collections.singleton("감성커피")))
                    .thenReturn(Collections.singletonList("카페/간식"));

            // when
            categoryService.categorizeTransactions(transactions, "some-key");

            // then
            // Gemini 서비스가 "감성커피"에 대해 한 번 호출되어야 함
            verify(geminiService).classifyCategories(Collections.singleton("감성커피"));

            ArgumentCaptor<List<TransactionHistoryVO>> captor = ArgumentCaptor.forClass(List.class);
            verify(transactionMapper).updateCategories(captor.capture());
            List<TransactionHistoryVO> updatedTransactions = captor.getValue();

            assertThat(updatedTransactions.get(0).getCategory_id()).isEqualTo(1L); // 스타벅스 -> 카페
            assertThat(updatedTransactions.get(1).getCategory_id()).isEqualTo(1L); // 감성커피 -> 카페/간식(1L)
        }

        @Test
        @DisplayName("상호명이 Redis에 캐시되어 있을 경우, DB나 API를 호출하지 않아야 한다")
        void categorizeTransactions_WhenDescriptionIsCached_ShouldUseCachedValue() {
            // given
            TransactionHistoryVO tx1 = new TransactionHistoryVO();
            tx1.setDescription("스타벅스");
            List<TransactionHistoryVO> transactions = Collections.singletonList(tx1);

            // Redis에 "스타벅스"가 카테고리 ID 1로 캐시되어 있다고 설정
            when(utilService.isStoredInRedis(RedisKeyUtil.category("스타벅스"))).thenReturn(1L);

            // when
            categoryService.categorizeTransactions(transactions, "some-key");

            // then
            // 내부 분류나 Gemini API는 호출되지 않아야 함
            verify(utilService, never()).mapToInternalCategory(anyString());
            verify(geminiService, never()).classifyCategories(anySet());

            ArgumentCaptor<List<TransactionHistoryVO>> captor = ArgumentCaptor.forClass(List.class);
            verify(transactionMapper).updateCategories(captor.capture());
            assertThat(captor.getValue().get(0).getCategory_id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Category 조회/생성/수정 메소드 테스트")
    class CrudMethodsTest {

        @Test
        @DisplayName("getCategoryById 호출 시, 존재하지 않는 ID이면 NoSuchElementException을 발생시켜야 한다")
        void getCategoryById_WhenNotFound_ShouldThrowException() {
            // given
            Long nonExistentId = 999L;
            when(categoryMapper.getCategoryById(nonExistentId)).thenReturn(null);

            // when & then
            assertThrows(NoSuchElementException.class, () -> {
                categoryService.getCategoryById(nonExistentId);
            });
        }

        @Test
        @DisplayName("createCategory 호출 시, 이미 존재하는 카테고리이면 DB에 새로 생성하지 않고 기존 카테고리를 반환해야 한다")
        void createCategory_WhenCategoryExists_ShouldReturnExistingOne() {
            // given
            String categoryName = "식비";
            CategoryVO existingCategory = CategoryVO.builder().id(3L).name(categoryName).build();
            when(categoryMapper.getCategoryByName(categoryName)).thenReturn(existingCategory);

            // when
            CategoryDTO result = categoryService.createCategory(categoryName);

            // then
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getName()).isEqualTo(categoryName);
            // createCategory는 호출되지 않아야 함
            verify(categoryMapper, never()).createCategory(any(CategoryVO.class));
        }

        @Test
        @DisplayName("createCategory 호출 시, 존재하지 않는 카테고리이면 DB에 새로 생성하고 반환해야 한다")
        void createCategory_WhenCategoryNotExist_ShouldCreateAndReturn() {
            // given
            String newCategoryName = "새 카테고리";
            CategoryVO newCategoryVO = CategoryVO.builder().id(10L).name(newCategoryName).build();

            // 처음 이름으로 조회 시에는 null 반환 (존재하지 않음)
            when(categoryMapper.getCategoryByName(newCategoryName)).thenReturn(null);
            // 생성 후 ID로 조회 시에는 생성된 객체 반환
            when(categoryMapper.getCategoryById(anyLong())).thenReturn(newCategoryVO);

            // when
            CategoryDTO result = categoryService.createCategory(newCategoryName);

            // then
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo(newCategoryName);
            // createCategory가 한 번 호출되었는지 검증
            verify(categoryMapper).createCategory(any(CategoryVO.class));
        }
    }
}

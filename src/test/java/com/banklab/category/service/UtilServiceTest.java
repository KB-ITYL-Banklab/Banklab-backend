package com.banklab.category.service;

import com.banklab.common.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * UtilService에 대한 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class UtilServiceTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UtilService utilService;

    @Nested
    @DisplayName("Redis 관련 메소드 테스트")
    class RedisMethodsTest {

        @Test
        @DisplayName("isStoredInRedis 호출 시, Redis에 캐시된 카테고리 ID가 있으면 Long 타입으로 반환해야 한다")
        void isStoredInRedis_WhenCategoryIsCached_ShouldReturnCategoryIdAsLong() {
            // given: 테스트를 위한 전제 조건 설정
            String redisKey = "category:스타벅스";
            String cachedCategoryId = "1";
            when(redisService.get(redisKey)).thenReturn(cachedCategoryId);

            // when: 테스트할 메소드 호출
            Long categoryId = utilService.isStoredInRedis(redisKey);

            // then: 결과 검증
            assertThat(categoryId).isEqualTo(1L);
            verify(redisService).get(redisKey);
        }

        @Test
        @DisplayName("isStoredInRedis 호출 시, Redis에 캐시된 값이 없으면 null을 반환해야 한다")
        void isStoredInRedis_WhenCategoryIsNotCached_ShouldReturnNull() {
            // given
            String redisKey = "category:새로운가게";
            when(redisService.get(redisKey)).thenReturn(null);

            // when
            Long categoryId = utilService.isStoredInRedis(redisKey);

            // then
            assertThat(categoryId).isNull();
            verify(redisService).get(redisKey);
        }

        @Test
        @DisplayName("isStoredInRedis 호출 시, 캐시된 값이 숫자로 변환될 수 없으면 null을 반환해야 한다")
        void isStoredInRedis_WhenCachedValueIsInvalid_ShouldReturnNull() {
            // given
            String redisKey = "category:잘못된값";
            String invalidCachedValue = "not-a-number";
            when(redisService.get(redisKey)).thenReturn(invalidCachedValue);

            // when
            Long categoryId = utilService.isStoredInRedis(redisKey);

            // then
            assertThat(categoryId).isNull();
        }

        @Test
        @DisplayName("storeInRedis 호출 시, RedisService의 set 메소드를 정확한 인자와 함께 호출해야 한다")
        void storeInRedis_ShouldCallRedisServiceSet() {
            // given
            String redisKey = "category:스타벅스";
            String categoryId = "1";

            // when
            utilService.storeInRedis(redisKey, categoryId);

            // then
            // redisService.set이 정확한 인자들(key, value, expiration)로 호출되었는지 검증
            verify(redisService).set(redisKey, categoryId, 30);
        }
    }

    @Nested
    @DisplayName("mapToInternalCategory 메소드 테스트")
    class MapToInternalCategoryTest {

        // 파라미터화된 테스트를 사용하여 여러 입력값과 기대값을 한 번에 테스트
        @ParameterizedTest
        @CsvSource({
                "스타벅스, 1", // 카페/간식
                "메가커피, 1",
                "SKT, 2",     // 주거/통신
                "KT 통신요금, 2",
                "배달의민족, 3", // 식비
                "BHC, 3",
                "카카오택시, 4", // 교통
                "주유소, 4",
                "올리브영, 5", // 쇼핑
                "무신사, 5",
                "CGV, 6",     // 문화/여가
                "메가박스, 6",
                "김민준, 7",   // 이체 (사람 이름)
                "토스, 7",
                "알수없는가게, 8" // 기타
        })
        @DisplayName("다양한 상호명에 대해 정확한 카테고리 ID를 반환해야 한다")
        void mapToInternalCategory_WithVariousNames_ShouldReturnCorrectCategoryId(String description, long expectedCategoryId) {
            // given: 파라미터로 상호명(description)이 주어짐

            // when: 테스트할 메소드 호출
            long actualCategoryId = utilService.mapToInternalCategory(description);

            // then: 결과 검증
            assertThat(actualCategoryId).isEqualTo(expectedCategoryId);
        }

        @Test
        @DisplayName("어떤 키워드에도 해당하지 않는 상호명은 '기타' 카테고리(8)로 분류되어야 한다")
        void mapToInternalCategory_WithUnclassifiedName_ShouldReturnEtcCategory() {
            // given
            String description = "정말분류하기어려운상호명";

            // when
            long categoryId = utilService.mapToInternalCategory(description);

            // then
            assertThat(categoryId).isEqualTo(8L);
        }
    }
}

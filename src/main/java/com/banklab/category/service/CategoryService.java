package com.banklab.category.service;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.gemini.service.GeminiService;
import com.banklab.category.mapper.CategoryMapper;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;
    private final GeminiService geminiService;
    private final UtilService utilService;

    /**
     * 거래 내역 리스트를 받아 각 거래의 상호명을 분석하여 카테고리를 분류하고 저장합니다.
     * 처리 순서:
     * 1. Redis 캐시에서 상호명에 해당하는 카테고리가 있는지 확인합니다.
     * 2. 캐시에 없으면, 내부 키워드 매칭(UtilService)을 병렬로 시도합니다.
     * 3. 내부 매칭으로 분류되지 않은 상호명들은 Gemini API를 통해 일괄 조회하여 분류합니다.
     * 4. 최종 분류된 결과를 거래 내역에 적용하고 DB에 저장합니다.
     * @param transactions 카테고리를 분류할 거래 내역(TransactionHistoryVO) 리스트
     * @param key Redis에서 작업 상태를 추적하기 위한 키 (현재는 사용되지 않음)
     */
    public void categorizeTransactions(List<TransactionHistoryVO> transactions, String key) {
        List<String> descriptions = transactions.stream()
                .map(TransactionHistoryVO::getDescription)
                .filter(Objects::nonNull) // null 방어
                .map(String::trim)        // 앞뒤 공백 제거
                .filter(s -> !s.isEmpty())// 빈 문자열 제거
                .distinct()
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(8); // 적절한 풀 사이즈
        Map<String, CompletableFuture<Long>> futureMap = new HashMap<>();
        
        // 동시성에 안전한 구조
        Map<String, Long> descMap = new ConcurrentHashMap<>();
        Set<String> toClassifyViaApi = Collections.synchronizedSet(new LinkedHashSet<>());

        for (String desc : descriptions) {
            String redisKey = RedisKeyUtil.category(desc);

            // 1. Redis 캐시 확인 (동기)
            Long categoryId = utilService.isStoredInRedis(redisKey);
            if (categoryId != null) {
                descMap.put(desc, categoryId);
                continue;
            }

            CompletableFuture<Long> future = CompletableFuture
                    .supplyAsync(() -> utilService.mapToInternalCategory(desc), executor)
                    .thenApplyAsync(category -> {
                        if (category != 8L) {
                            utilService.storeInRedis(redisKey, String.valueOf(category));
                            descMap.put(desc, category);
                        } else {
                            synchronized (toClassifyViaApi) {
                                toClassifyViaApi.add(desc);
                            }
                        }
                        return category;
                    }, executor);

            futureMap.put(desc, future);
        }

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // 4. 매핑 안 된 목록 GEMINI 호출
        if (!toClassifyViaApi.isEmpty()) {
            List<String> geminiResponses = geminiService.classifyCategories(toClassifyViaApi);

            // SET -> List
            List<String> toClassifyList = new ArrayList<>(toClassifyViaApi);
            int limit = Math.min(toClassifyList.size(), geminiResponses.size());

            for (int i = 0; i < limit; i++) {
                String desc = toClassifyList.get(i);
                String cName = geminiResponses.get(i);
                long categoryId = convertCategoryNameToId(cName);

                String redisKey = RedisKeyUtil.category(desc);
                descMap.put(desc, categoryId);
                utilService.storeInRedis(redisKey, String.valueOf(categoryId));
            }
        }
        // 카테고리 저장
        saveCategories(transactions, descMap);
    }

    private long convertCategoryNameToId(String categoryName) {
        return switch (categoryName) {
            case "카페/간식" -> 1;
            case "주거/통신" -> 2;
            case "식비" -> 3;
            case "교통" -> 4;
            case "쇼핑" -> 5;
            case "문화/여가" -> 6;
            case "이체" -> 7;
            default -> 8; // 기타
        };
    }

    /**
     * @param transactions CODEF에서 받아온 거래 내역
     * @param descMap      KEY: 상호명, VALUE: 카테고리
     */
    private void saveCategories(List<TransactionHistoryVO> transactions, Map<String, Long> descMap) {
        log.info("[START] 카테고리 저장 시작 : Thread: {}", Thread.currentThread().getName());

        for (TransactionHistoryVO tx : transactions) {
            String desc = tx.getDescription();
            Long categoryId = descMap.get(desc);

            if (categoryId == null) {
                log.warn("descMap에 결과 없음: {}", desc);
                tx.setCategory_id(8L);  // fallback: 기타 카테고리
                continue;
            }
            try {
                tx.setCategory_id(categoryId);
            } catch (CompletionException e) {   // 내부 작업에서 예외 발생 시
                log.warn("카테고리 분류 실패: {}, fallback 적용", desc, e.getCause());
                tx.setCategory_id(8L);
            } catch (Exception e) {     // 혹시 모를 다른 예외 처리
                log.error("카테고리 저장 중 예기치 못한 에러 발생: {}", desc, e);
                tx.setCategory_id(8L);
            }
        }
        transactionMapper.updateCategories(transactions);
        log.info("[END] 카테고리 저장 완료 : Thread: {}", Thread.currentThread().getName());
    }

    public List<CategoryDTO> findAll() {
        return categoryMapper.findAll().stream()
                .map(CategoryDTO::of)
                .collect(Collectors.toList());
    }

    /**
     * @param id 카테고리 아이디
     * @return 해당 카테고리 아이디를 가진 카테고리
     */
    public CategoryDTO getCategoryById(Long id) {
        return Optional.ofNullable(CategoryDTO.of(categoryMapper.getCategoryById(id)))
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * @param name 카테고리 이름
     * @return 해당 카테고리 이름을 가진 카테고리
     */
    public CategoryDTO getCategoryByName(String name) {
        return Optional.ofNullable(CategoryDTO.of(categoryMapper.getCategoryByName(name)))
                .orElseThrow(NoSuchElementException::new);
    }


    public CategoryDTO createCategory(String name) {
        try {
            return getCategoryByName(name);
        } catch (NoSuchElementException e) {
            CategoryVO vo = CategoryVO.builder().name(name).build();

            categoryMapper.createCategory(vo);
            return getCategoryById(vo.getId());
        }
    }

    public CategoryDTO updateCategory(CategoryDTO dto) {
        CategoryDTO category = getCategoryByName(dto.getName());

        // 업데하려는 카테고리 이름이 없는 경우 null 반환
        if (category == null) {
            return null;
        }

        CategoryVO categoryVO = dto.toVO();
        categoryMapper.updateCategory(categoryVO);

        return getCategoryById(categoryVO.getId());
    }
}

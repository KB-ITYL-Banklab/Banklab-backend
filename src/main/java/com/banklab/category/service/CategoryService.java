package com.banklab.category.service;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.gemini.service.GeminiService;
import com.banklab.category.kakaomap.service.KakaoMapService;
import com.banklab.category.mapper.CategoryMapper;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.summary.service.SummaryBatchService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
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
    private final KakaoMapService kakaoMapService;
    private final GeminiService geminiService;

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

        log.info("내부 필터링 병렬 처리 시작");
        Instant start = Instant.now();
        for (String desc : descriptions) {
            String redisKey = RedisKeyUtil.category(desc);

            // 1. Redis 캐시 확인 (동기)
            Long categoryId = kakaoMapService.isStoredInRedis(redisKey);
            if (categoryId != null) {
                descMap.put(desc, categoryId);
                continue;
            }

            CompletableFuture<Long> future = CompletableFuture
                    .supplyAsync(() -> kakaoMapService.mapToInternalCategory(desc), executor)
                    .thenApplyAsync(category -> {
                        if (category != 8L) {
                            kakaoMapService.storeInRedis(redisKey, String.valueOf(category));
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

        Instant end = Instant.now();
        long elapsedMillis = Duration.between(start, end).toMillis();
        log.info("내부 필터링 완료 - 전체 소요 시간: {} ms", elapsedMillis);


        log.info("API 호출로 분류해야 하는 상호명 개수: {}", toClassifyViaApi.size());
        log.info("======여기서 최대 1분 소요됩니다.======");

        // 4. 매핑 안 된 목록 GEMINI 호출
        if (!toClassifyViaApi.isEmpty()) {
            log.info("GEMINI 호출 시작");
            start = Instant.now();

            List<String> geminiResponses = geminiService.classifyCategories(toClassifyViaApi);

            end = Instant.now();
            elapsedMillis = Duration.between(start, end).toMillis();
            log.info("GEMINI 호출 완료 - 소요 시간: {} ms", elapsedMillis);


            // SET -> List
            List<String> toClassifyList = new ArrayList<>(toClassifyViaApi);
            int limit = Math.min(toClassifyList.size(), geminiResponses.size());
            log.warn("toClassifyViaApi size: {}, geminiResponses size: {}", toClassifyViaApi.size(), geminiResponses.size());

            for (int i = 0; i < limit; i++) {
                String desc = toClassifyList.get(i);
                String cName = geminiResponses.get(i);
                long categoryId = convertCategoryNameToId(cName);

                String redisKey = RedisKeyUtil.category(desc);
                descMap.put(desc, categoryId);
                kakaoMapService.storeInRedis(redisKey, String.valueOf(categoryId));
            }
        }

        /** Kakao API
         for(String desc : toClassifyViaApi){
         String redisKey = "category::"+desc;
         try{
         long categoryId = kakaoMapService.getCategoryByDesc(redisKey, desc);
         descMap.put(desc, categoryId);
         }catch (Exception e){
         log.error("카테고리 분류 중 에러 발생");
         }
         /** 비동기 처리
         descMap.put(desc,
         CompletableFuture.supplyAsync(() -> {
         try {
         // 동시에 실행되는 작업이 10개 초과인 경우 대기 (병렬 제한)
         concurrentLimit.acquire();
         rateLimiter.acquire();  // 초당 요청 수 제한 (속도 제한)
         getCategoryWithCache(desc);
         return getCategoryWithCache(desc);  // 실제 분류
         } catch (Exception e) {
         log.error("카테고리 분류 api 호출 중 에러 발생", e);
         throw new RuntimeException(e);
         }finally {
         concurrentLimit.release();
         }
         }, asyncExecutor)
         );

         }
         */
        // 이제 모든 분류 끝났으니 바로 저장 호출
        saveCategories(transactions, descMap);
//        // 비동기 작업이 다 끝난 경우 DB 저장 (전체 분류가 다 끝난 경우)
//        CompletableFuture<Void> allDone = CompletableFuture.allOf(descMap.values().toArray(new CompletableFuture[0]));
//        return allDone.thenRun(() -> saveCategories(transactions, descMap));
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

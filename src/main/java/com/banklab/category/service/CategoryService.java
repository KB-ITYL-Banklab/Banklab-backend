package com.banklab.category.service;

import com.banklab.category.domain.CategoryVO;
import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.kakaomap.service.KakaoMapService;
import com.banklab.category.mapper.CategoryMapper;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryService {

    @Qualifier("asyncExecutor")
    private final Executor asyncExecutor;

    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;
    private final KakaoMapService kakaoMapService;
    private final RedisService redisService;

    @Async
    public  CompletableFuture<Void> categorizeTransactions(List<TransactionHistoryVO> transactions, String key) {
        List<String> descriptions = transactions.stream()
                .map(TransactionHistoryVO::getDescription)
                .distinct()
                .toList();
        // 상호명 -> 분류 결과를 비동기적으로 담을 맵
        Map<String, CompletableFuture<Long>> descMap = new HashMap<>();
        log.info("[START] 카테고리 분류 시작:  Thread: {}", Thread.currentThread().getName());
        
        // 프론트에 현재 카테고리 분류 알림 (TTL 2분)
        redisService.set(key, "CLASSIFYING_CATEGORIES",2);

        RateLimiter rateLimiter = RateLimiter.create(0.7);   // 호출 순서 조정 2초에 1번
        Semaphore concurrentLimit = new Semaphore(10); // 동시에 최대 10개만 병렬 처리

        for (int i = 0; i < descriptions.size(); i++) {
            String desc = descriptions.get(i);
            // desc를 백그라운드에서 병렬 처리, 최대 10개, 초당 20개 이하
            descMap.put(desc,
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            // 동시에 실행되는 작업이 10개 초과인 경우 대기 (병렬 제한)
                            concurrentLimit.acquire();
                            rateLimiter.acquire();  // 초당 요청 수 제한 (속도 제한)
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

        // 비동기 작업이 다 끝난 경우 DB 저장 (전체 분류가 다 끝난 경우)
        CompletableFuture<Void> allDone = CompletableFuture.allOf(descMap.values().toArray(new CompletableFuture[0]));
        return allDone.thenRun(() -> saveCategories(transactions, descMap));
    }

    /**
     * @param transactions CODEF에서 받아온 거래 내역
     * @param descMap   KEY: 상호명, VALUE: 카테고리
     */
    private void saveCategories(List<TransactionHistoryVO> transactions, Map<String, CompletableFuture<Long>> descMap) {
        log.info("[START] 카테고리 저장 시작 : Thread: {}", Thread.currentThread().getName());

        for (TransactionHistoryVO tx : transactions) {
            String desc = tx.getDescription();
            CompletableFuture<Long> future = descMap.get(desc);

            if (future == null) {
                log.warn("descMap에 결과 없음: {}", desc);
                tx.setCategory_id(8L);  // fallback: 기타 카테고리
                continue;
            }
            try {
                Long categoryId = future.join(); // join()은 이미 완료된 Future라면 즉시 결과 반환
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


    /**
     * 
     * @param keyword 해당 상호명 Redis 저장 확인
     * @return  저장된 경우 Redis 값 반환, 아니면 상호면 분류
     */
    public Long getCategoryWithCache(String keyword){
        String redisKey = "category::"+keyword;

        // 1. Redis 캐시 확인
        Long categoryId = kakaoMapService.isStoredInRedis(redisKey);
        if (categoryId!=null){
            return categoryId;
        }

        categoryId = kakaoMapService.getCategoryByDesc(redisKey, keyword);
        return categoryId;
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

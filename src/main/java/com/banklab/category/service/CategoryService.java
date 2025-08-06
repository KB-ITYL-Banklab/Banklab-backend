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

    public void categorizeTransactions(List<TransactionHistoryVO> transactions, String key) {
        List<String> descriptions = transactions.stream()
                .map(TransactionHistoryVO::getDescription)
                .filter(Objects::nonNull) // null 방어
                .map(String::trim)        // 앞뒤 공백 제거
                .filter(s -> !s.isEmpty())// 빈 문자열 제거
                .distinct()
                .toList();

        // 상호명 -> 분류 결과를 담을 맵
        Map<String, Long> descMap = new HashMap<>();
        log.info("[START] 카테고리 분류 시작:  Thread: {}", Thread.currentThread().getName());
        
        // 프론트에 현재 카테고리 분류 알림 (TTL 2분)
        redisService.set(key, "CLASSIFYING_CATEGORIES",2);
        List<String> toClassifyViaKakaoApi = new ArrayList<>();

        // 먼저 매핑
        for(String desc : descriptions){
            String redisKey = "category::"+desc;
            // 1. Redis 캐시 확인
            Long categoryId = kakaoMapService.isStoredInRedis(redisKey);
            if (categoryId!=null){
                descMap.put(desc, categoryId);
                continue;
            }

            // 2.내부 필터링
            categoryId = kakaoMapService.mapToInternalCategory(desc);
            if (categoryId != 8) {
                kakaoMapService.storeInRedis(redisKey, String.valueOf(categoryId));
                descMap.put(desc, categoryId);
                continue;
            }
            // 3. Redis에도 없고 내부 매핑도 못한 애들만 모음
            toClassifyViaKakaoApi.add(desc);
        }

        log.info("API 호출로 분류해야 하는 상호명 개수: {}", toClassifyViaKakaoApi.size());
        log.info("===================여기서 최대 3분 소요됩니다.===================");

        for(String desc : toClassifyViaKakaoApi){
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
             */
        }
        // 이제 모든 분류 끝났으니 바로 저장 호출
        saveCategories(transactions, descMap);
//        // 비동기 작업이 다 끝난 경우 DB 저장 (전체 분류가 다 끝난 경우)
//        CompletableFuture<Void> allDone = CompletableFuture.allOf(descMap.values().toArray(new CompletableFuture[0]));
//        return allDone.thenRun(() -> saveCategories(transactions, descMap));
    }

    /**
     * @param transactions CODEF에서 받아온 거래 내역
     * @param descMap   KEY: 상호명, VALUE: 카테고리
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

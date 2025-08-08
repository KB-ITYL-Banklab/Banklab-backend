package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.account.domain.AccountVO;
import com.banklab.category.kakaomap.service.KakaoMapService;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConfig;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.CategorizeTransactionMessage;
import com.banklab.transaction.rabbitMQ.message.GeminiCategorizeMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Log4j2
public class CategorizeTransactionConsumer {
    private final KakaoMapService kakaoMapService;
    private final TransactionProducer transactionProducer;
    private final RedisService redisService;

    @RabbitListener(queues = RabbitMQConstant.QUEUE_CATEGORIZE_INTERNAL)
    public void handleTransactionCategorize(CategorizeTransactionMessage message){
        List<TransactionHistoryVO> transactions = message.getTransactions();
        List<String> descriptions = getDescriptions(transactions);
        Long memberId = message.getMemberId();
        AccountVO account = message.getAccount();

        String key = RedisKeyUtil.category(account.getId());
        String statusKey = RedisKeyUtil.transaction(memberId, account.getResAccount());
        redisService.set(statusKey, "CLASSIFYING_CATEGORIES", 1);

        log.info("[RQ] 카테고리 분류 요청 수신, 건수={}", message.getTransactions().size());
        redisService.hset(key, "expectedTotal", String.valueOf(message.getTransactions().size()));

        Map<String, Long> descMap = new ConcurrentHashMap<>();
        Set<String> toClassifyViaApi = Collections.synchronizedSet(new LinkedHashSet<>());

        descriptions.parallelStream().forEach(dx->{
            long categoryId = kakaoMapService.mapToInternalCategory(dx);
            if(categoryId!=8L){
                descMap.put(dx, categoryId);
                // Redis 해시에 저장
                redisService.hset(key, dx, String.valueOf(categoryId));
            }else{
                toClassifyViaApi.add(dx);
            }
        });

        log.info("[END] 카테고리 내부 매핑 성공, 건수: {}",descMap.size());
        
        if(!toClassifyViaApi.isEmpty()){
            // GEMINI 큐에 전달
            GeminiCategorizeMessage geminiCategorizeMessage
                    = new GeminiCategorizeMessage(memberId, account, message.getStartDate(), toClassifyViaApi, transactions);
            transactionProducer.sendExternalCategoryRequest(geminiCategorizeMessage);
        }
    }

    // 상호명 추출 & 중복 제거
    private List<String> getDescriptions(List<TransactionHistoryVO> transactions){
        return  transactions.stream()
                .map(TransactionHistoryVO::getDescription)
                .filter(Objects::nonNull) // null 방어
                .map(String::trim)        // 앞뒤 공백 제거
                .filter(s -> !s.isEmpty())// 빈 문자열 제거
                .distinct()
                .toList();
    }
}

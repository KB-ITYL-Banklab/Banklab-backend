package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.category.gemini.service.GeminiService;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.rabbitMQ.message.GeminiCategorizeMessage;
import com.banklab.transaction.rabbitMQ.message.SaveCategoryMessage;
import com.banklab.transaction.rabbitMQ.message.SummaryTransactionMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Log4j2
public class GeminiCategorizationConsumer {
    private final TransactionProducer transactionProducer;
    private final GeminiService geminiService;
    private final RedisService redisService;

    @RabbitListener(queues = "transaction.gemini")
    public void handleGeminiCategorize(GeminiCategorizeMessage message) {
        Set<String> descriptions = message.getDescriptions();
        Long accountId = message.getAccountId();

        log.info("[RQ] GEMINI 분류 요청 수신, 건수={}", descriptions.size());
        List<String> geminiResponses = geminiService.classifyCategories(descriptions);
        String key = RedisKeyUtil.category(accountId);

        // SET -> List
        List<String> toClassifyList = new ArrayList<>(descriptions);
        int limit = Math.min(toClassifyList.size(), geminiResponses.size());
        log.warn("toClassifyViaApi size: {}, geminiResponses size: {}", toClassifyList.size(), geminiResponses.size());

        for (int i = 0; i < limit; i++) {
            String desc = toClassifyList.get(i);
            String cName = geminiResponses.get(i);
            long categoryId = convertCategoryNameToId(cName);
            redisService.hset(key, desc, String.valueOf(categoryId));
        }

        // 예상 분류 건수
        long expected = Long.parseLong(redisService.hget(key, "expectedTotal"));
        log.info("[END] GEMINI 분류 성공, 건수: {}", geminiResponses.size());

        SaveCategoryMessage saveCategoryMessage = new SaveCategoryMessage(message.getMemberId(), accountId, message.getStartDate(), message.getTransactions());
        transactionProducer.

//
//        // Summary 큐에 전달
//        SummaryTransactionMessage summaryTransactionMessage =
//                new SummaryTransactionMessage(message.getMemberId(), message.getStartDate());
//        transactionProducer.sendTransactionSummaryRequest(summaryTransactionMessage);

    }

    public boolean isCategorizationComplete(String redisKey, Long expectedCount) {
        Long currentCount = redisService.hlen(redisKey);
        return currentCount != null && (currentCount-1) >= expectedCount;
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

}

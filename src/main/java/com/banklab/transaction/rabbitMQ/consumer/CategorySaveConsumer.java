package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.rabbitMQ.message.SaveCategoryMessage;
import com.banklab.transaction.rabbitMQ.message.SummaryTransactionMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;


@Component
@RequiredArgsConstructor
@Log4j2
public class CategorySaveConsumer {
    private final RedisService redisService;
    private final TransactionMapper transactionMapper;
    private final TransactionProducer transactionProducer;


    @RabbitListener(queues = "transaction.category.save")
    public void handleSaveCategories(SaveCategoryMessage message) {
        Long accountId = message.getAccountId();
        List<TransactionHistoryVO> transactions = message.getTransactions();

        log.info("[RQ] 카테고리 저장 요청 수신: 계좌ID:{}", accountId);

        String key = RedisKeyUtil.category(accountId);
        Map<Object, Object> descMap = redisService.hgetAll(key);

        for (TransactionHistoryVO tx : transactions) {
            String desc = tx.getDescription();
            Long categoryId = Long.parseLong(String.valueOf(descMap.get(desc)));
            try {
                tx.setCategory_id(categoryId);
            } catch (CompletionException e) {
                log.warn("카테고리 분류 실패: {}, 기타 분류 적용", desc, e.getCause());
                tx.setCategory_id(8L);
            } catch (Exception e) {     // 혹시 모를 다른 예외 처리
                log.error("카테고리 저장 중 예기치 못한 에러 발생: {}", desc, e);
                tx.setCategory_id(8L);
            }
        }
        transactionMapper.updateCategories(transactions);
        
        // 집계 테이블 업데이트 큐 전달
        SummaryTransactionMessage summaryTransactionMessage =
                new SummaryTransactionMessage(message.getMemberId(), message.getStartDate());
        transactionProducer.sendTransactionSummaryRequest(summaryTransactionMessage);
        log.info("[SEND] 집계 테이블 업데이트 큐 전송 완료: 계좌ID:{}", accountId);
    }


}

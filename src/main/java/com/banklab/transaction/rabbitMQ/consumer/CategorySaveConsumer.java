package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.account.domain.AccountVO;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConfig;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.SaveCategoryMessage;
import com.banklab.transaction.rabbitMQ.message.SummaryTransactionMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
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


    @RabbitListener(queues = RabbitMQConstant.QUEUE_CATEGORY_SAVE)
    public void handleSaveCategories(SaveCategoryMessage message) {
        AccountVO account = message.getAccount();
        List<TransactionHistoryVO> transactions = message.getTransactions();

        log.info("[RQ] 카테고리 저장 요청 수신: 계좌:{}", account.getResAccount());

        String key = RedisKeyUtil.category(account.getId());
        Map<Object, Object> descMap = redisService.hgetAll(key);

        for (TransactionHistoryVO tx : transactions) {
            String desc = tx.getDescription();
            Long categoryId = Long.parseLong(String.valueOf(descMap.get(desc)));
            try {
                tx.setCategory_id(categoryId);
            } catch (CompletionException e) {
                log.warn("카테고리 분류 실패: {}, 기타 분류 적용", desc, e.getCause());
                tx.setCategory_id(8L);
            } catch (Exception e) {
                log.error("카테고리 저장 중 예기치 못한 에러 발생: {}", desc, e);
                tx.setCategory_id(8L);
            }
        }
        transactionMapper.updateCategories(transactions);
        
        // 집계 테이블 업데이트 큐 전달
        SummaryTransactionMessage summaryTransactionMessage =
                new SummaryTransactionMessage(message.getMemberId(),account,message.getStartDate());
        transactionProducer.sendTransactionSummaryRequest(summaryTransactionMessage);
        log.info("[SEND] 집계 테이블 업데이트 큐 전송 완료: 계좌:{}", account.getResAccount());
    }


}

package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConfig;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.SummaryTransactionMessage;
import com.banklab.transaction.summary.service.SummaryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class SummaryTransactionConsumer {

    private final SummaryBatchService summaryBatchService;
    private final RedisService redisService;

    @RabbitListener(queues = RabbitMQConstant.QUEUE_SUMMARY_SAVE)
    public void handleSummarySave(SummaryTransactionMessage message){
        Long memberId = message.getMemberId();
        String startDate = message.getStartDate();
        log.info("[RQ] 집계 데이터 저장 요청 수신: 회원ID={}, 시작일={}", memberId,startDate);

        String key = RedisKeyUtil.transaction(memberId, message.getAccountVO().getResAccount());
        redisService.set(key, "ANALYZING_DATA", 2);
        try {
            summaryBatchService.initDailySummary(memberId, startDate);
            log.info("[END] 집계 데이터 저장 요청 완료: 회원ID={}, 시작일={}", memberId,startDate);
            redisService.set(key, "DONE", 1);
        }catch (Exception e){
            redisService.set(key, "FAILED", 1);
        }

    }
}

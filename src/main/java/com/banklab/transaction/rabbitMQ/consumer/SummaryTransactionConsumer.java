package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.transaction.rabbitMQ.message.SummaryTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class SummaryTransactionConsumer {

    @RabbitListener(queues = "transaction.summary")
    public void handleSummarySave(SummaryTransactionMessage message){
        Long memberId = message.getMemberId();
        String startDate = message.getStartDate();
        log.info("[RQ] 집계 데이터 저장 요청 수신: 회원ID={}, 시작일={}", memberId,startDate);


        log.info("[END] 집계 데이터 저장 요청 완료: 회원ID={}, 시작일={}", memberId,startDate);




    }
}

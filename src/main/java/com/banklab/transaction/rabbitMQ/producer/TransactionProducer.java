package com.banklab.transaction.rabbitMQ.producer;


import com.banklab.config.RabbitMQConfig;
import com.banklab.transaction.rabbitMQ.message.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProducer {
    private final RabbitTemplate rabbitTemplate;

    // 1. 거래 내역 외부 API 조회 요청
    public void sendTransactionFetchReqeust(FetchTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_TRANSACTION_FETCH, message);
    }

    // 2. 거래 내역 저장 요청
    public void sendTransactionSaveReqeust(SaveTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_TRANSACTION_SAVE, message);
    }

    // 3. 카테고리 내부 매핑 요청
    public void sendTransactionCategorizationReqeust(CategorizeTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_CATEGORIZE, message);
    }

    // 4. 카테고리 GEMINI 요청
    public void sendGeminicationRequest(GeminiCategorizeMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_GEMINI, message);
    }

    // 5. 카테고리 저자 요청
    public void sendSaveCategoryRequest(SaveCategoryMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_CATEGORY_SAVE, message);
    }


    // 4. 집계 테이블 저장 요청 - 미완
    public void sendTransactionSummaryRequest(SummaryTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SUMMARY, message);
    }






}

package com.banklab.transaction.rabbitMQ.producer;


import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProducer {
    private final RabbitTemplate rabbitTemplate;

    // 1. 거래 내역 외부 API 조회 요청
    public void sendTransactionFetchRequest(FetchTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE, RabbitMQConstant.ROUTING_TRANSACTION_FETCH, message);
    }

    // 2. 거래 내역 저장 요청
    public void sendTransactionSaveRequest(SaveTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE,RabbitMQConstant.ROUTING_TRANSACTION_SAVE, message);
    }

    // 3. 카테고리 내부 매핑 요청
    public void sendInternalCategoryRequest(CategorizeTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE,RabbitMQConstant.ROUTING_CATEGORIZE_INTERNAL, message);
    }

    // 4. 카테고리 GEMINI 요청
    public void sendExternalCategoryRequest(GeminiCategorizeMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE,RabbitMQConstant.ROUTING_CATEGORIZE_EXTERNAL, message);
    }

    // 5. 카테고리 저장 요청
    public void sendSaveCategoryRequest(SaveCategoryMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE,RabbitMQConstant.ROUTING_CATEGORY_SAVE, message);
    }

    // 4. 집계 테이블 저장 요청
    public void sendTransactionSummaryRequest(SummaryTransactionMessage message){
        rabbitTemplate.convertAndSend(RabbitMQConstant.EXCHANGE,RabbitMQConstant.ROUTING_SUMMARY_SAVE, message);
    }
}

package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.account.domain.AccountVO;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConfig;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.CategorizeTransactionMessage;
import com.banklab.transaction.rabbitMQ.message.SaveTransactionMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class SaveTransactionConsumer {

    private final TransactionService transactionService;
    private final TransactionProducer transactionProducer;

    @RabbitListener(queues = RabbitMQConstant.QUEUE_TRANSACTION_SAVE)
    public void handleTransactionSave(SaveTransactionMessage message){
        Long memberId = message.getMemberId();
        AccountVO account = message.getAccount();
        List<TransactionHistoryVO> transactions = message.getTransactions();

        log.info("[RQ] 거래 내역 저장 요청 수신: 회원ID={}, 계좌={}, 건수={}"
                , memberId, account.getResAccount(), transactions.size());

        try{
            transactionService.saveTransactionList(memberId, message.getAccount(),transactions);
            log.info("[DONE] 거래 내역 저장 성공:  회원ID={}, 계좌={}", memberId, account.getResAccount());
        }catch (Exception e){
            log.error("[ERROR] 거래 내역 저장 실패:  회원ID={}, 계좌={}", memberId, account.getResAccount());
        }

        // 카테고리 분류 큐에 전달
        CategorizeTransactionMessage categorizeTransactionMessage =
                new CategorizeTransactionMessage(memberId, account,message.getStartDate(), transactions);
        transactionProducer.sendInternalCategoryRequest(categorizeTransactionMessage);


        log.info("[SEND] 카테고리 분류 큐 전송 완료");
    }
}

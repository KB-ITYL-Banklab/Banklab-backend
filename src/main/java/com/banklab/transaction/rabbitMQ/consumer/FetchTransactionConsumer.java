package com.banklab.transaction.rabbitMQ.consumer;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.mapper.AccountMapper;
import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.mapper.TransactionMapper;
import com.banklab.transaction.rabbitMQ.config.RabbitMQConstant;
import com.banklab.transaction.rabbitMQ.message.FetchTransactionMessage;
import com.banklab.transaction.rabbitMQ.message.SaveTransactionMessage;
import com.banklab.transaction.rabbitMQ.producer.TransactionProducer;
import com.banklab.transaction.service.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchTransactionConsumer {
    private static final Logger log = LoggerFactory.getLogger(FetchTransactionConsumer.class);
    private final TransactionProducer transactionProducer;
    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final RedisService redisService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @RabbitListener(queues = RabbitMQConstant.QUEUE_TRANSACTION_FETCH)
    public void handleTransactionFetch(FetchTransactionMessage message){
        Long memberId = message.getMemberId();
        TransactionRequestDto request = message.getRequest();

        log.info("[RQ] 거래 내역 조회 요청 수신: account:{}", request.getResAccount());
        AccountVO account = accountMapper.getAccountByAccountNumber(request.getResAccount());
        if(account ==null){return;}

        checkIsPresent(memberId, account, request);
        TransactionDTO dto = makeTransactionDTO(account, request);

        String key = RedisKeyUtil.transaction(memberId, dto.getAccount());
        redisService.set(key, "FETCHING_TRANSACTIONS", 1);

        try{
            // CODEF 거래 내역 조회 API 호출
            List<TransactionHistoryVO> transactions
                    = TransactionResponse.requestTransactions(memberId, dto);
            if (transactions.isEmpty()) return;

            // 저장 큐에 전달
            SaveTransactionMessage transactionSaveMessage = new SaveTransactionMessage(memberId,account,dto.getStartDate(), transactions);
            transactionProducer.sendTransactionSaveRequest(transactionSaveMessage);

            log.info("[SEND] 거래 내역 저장 큐 전송 완료: 건수 {}", transactions.size());
        }catch (Exception e){
            log.warn("거래 내역 조회 중 오류 발생",e);
        }

    }

    public void checkIsPresent(Long memberId, AccountVO account, TransactionRequestDto req) {
        LocalDate lastTransactionDate =
                transactionMapper.getLastTransactionDate(memberId, account.getResAccount());

        if (lastTransactionDate != null) {
            if (req == null) req = new TransactionRequestDto();
            req.setStartDate(lastTransactionDate.format(formatter));
        }
    }

    /**
     * @param account 계좌 정보
     * @param request 거래 내역 조회를 위한 요청 파라미터 (sDate, eDate, orderBy)
     * @return 거래 내역 조회를 위한 요청 DTO
     */
    public TransactionDTO makeTransactionDTO(AccountVO account, TransactionRequestDto request) {
        if (request == null) {
            request = new TransactionRequestDto();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(2);

            request.setStartDate(startDate.format(formatter)); // "20190601" 형식
            request.setEndDate(endDate.format(formatter));     // 오늘 날짜 형식
            request.setOrderBy("0");
        } else {
            if (request.getStartDate() == null || request.getStartDate().isEmpty()) {
                LocalDate defaultStartDate = LocalDate.now().minusYears(2);
                request.setStartDate(defaultStartDate.format(formatter));
            }
            if (request.getEndDate() == null || request.getEndDate().isEmpty()) {
                LocalDate defaultEndDate = LocalDate.now();
                request.setEndDate(defaultEndDate.format(formatter));
            }

            if (request.getOrderBy() == null || request.getOrderBy().isEmpty()) {
                request.setOrderBy("0");
            }
        }

        return TransactionDTO.builder()
                .account(account.getResAccount())
                .organization(account.getOrganization())
                .connectedId(account.getConnectedId())
                .orderBy(request.getOrderBy())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

}

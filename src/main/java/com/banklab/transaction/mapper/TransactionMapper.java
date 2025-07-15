package com.banklab.transactions.mapper;

import com.banklab.codeapi.domain.TransactionHistoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TransactionMapper {
    // 거래 내역 불러오기
    List<TransactionHistoryVO> getTransactionList();

    Map<String, Long> getTransactionSummary(@Param("targetDate") String targetDate);

}
